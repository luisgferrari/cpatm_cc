package cc.service;

import cc.csv.Csv;
import cc.entity.Linha;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A classe {@code PlanilhaFlights} representa um arquivo CSV com registros de
 * voos exportados do SAGITARIO e fornece métodos para verificar a integridade
 * desses dados.
 *
 * <p>
 * Esta classe estende a classe {@code Planilha} e inclui constantes e métodos
 * específicos para a validação de arquivos CSV. O método principal
 * {@code verificarIntegridade} realiza várias verificações de integridade nos
 * dados do arquivo CSV e gera um relatório detalhado.</p>
 *
 * * @author luisg
 */
public class PlanilhaFlights extends Planilha {

    /**
     * O cabeçalho padrão para uma planilha flights. Este cabecalho define a
     * estrutura esperada dos dados na planilha.
     */
    public static final String CABECALHO = "timestamp;config_id;sect_config;CTR;ASS;sector;#sectors;#ASS;CALLSIGN;ADEP;ADES;DOF;EOBT;SSR;flrul;";
    /**
     * A quantidade de campos esperados em cada linha da planilha flights.
     * Calculada com base na quantidade de elementos separados por ponto e
     * vírgula no cabeçalho mais 1, devido ao campo timestamt utilizar ';' para
     * separar a DATE do TIME.
     */
    public static final int QTD_CAMPOS = CABECALHO.split(";").length;
    /**
     * O sufixo padrão para o nome do arquivo de planilha flights.
     */
    public static final String SUFIXO_ARQUIVO = "_flights.csv";

    /**
     * Verifica a integridade de um arquivo CSV e gera um relatório detalhado.
     * <p>
     * Este método lê um arquivo CSV, realiza várias verificações de integridade
     * nos dados, e gera um relatório contendo os resultados dessas
     * verificações. O relatório é salvo em um arquivo de saída com o mesmo nome
     * do arquivo de entrada, mas com extensão .txt. Em caso de erro durante a
     * leitura do arquivo, um relatório de erro é gerado e salvo em um arquivo
     * com extensão -ERRO.txt.
     * </p>
     *
     * @param inputFile o caminho para o arquivo CSV a ser verificado
     * @param detalharVerificacao caso true o método detalhará no
     * relatorioIntegridade todas as validações realizadas mesmo que não
     * encontre erros
     * @return uma lista de strings contendo o relatório de integridade
     */
    public static List<String> verificarIntegridade(Path inputFile, boolean detalharVerificacao) {
        List<String> relatorioIntegridade = new ArrayList<>();
        relatorioIntegridade.add("RELATÓRIO DE INTEGRIDADE - " + inputFile.getFileName().toString());

        try {
            List<Linha> linhasDoArquivo = Csv.lerLinhas(inputFile);

            localizarCabecalho(linhasDoArquivo, relatorioIntegridade, CABECALHO, detalharVerificacao);
            verificarQuantidadeDeCampos(linhasDoArquivo, relatorioIntegridade, QTD_CAMPOS, detalharVerificacao);
            validarLinhas(linhasDoArquivo, relatorioIntegridade, detalharVerificacao);

            Path outputFile = Paths.get(inputFile.getParent().toString().concat("\\Relatórios"), inputFile.getFileName().toString().replace(".csv", ".txt"));
            Csv.escrever(relatorioIntegridade, outputFile);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            Path outputFile = Paths.get(inputFile.getParent().toString().concat("\\Erro"), inputFile.getFileName().toString().replace(".csv", "-ERRO.txt"));
            relatorioIntegridade.add(e.getLocalizedMessage());
            Csv.escrever(relatorioIntegridade, outputFile);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            Path outputFile = Paths.get(inputFile.getParent().toString().concat("\\Erro"), inputFile.getFileName().toString().replace(".csv", "-ERRO.txt"));
            relatorioIntegridade.add(e.getLocalizedMessage());
            Csv.escrever(relatorioIntegridade, outputFile);
        }

        return relatorioIntegridade;
    }

    /**
     * Valida as linhas fornecidas e registra quaisquer erros de integridade no
     * relatório especificado.
     *
     * <p>
     * O método percorre a lista de linhas do arquivo, valida cada linha
     * individualmente e, se encontrar algum erro, adiciona esse erro e a linha
     * correspondente às listas de erros e linhas inválidas, respectivamente. No
     * final, o método adiciona ao relatório de integridade uma mensagem
     * indicando se houve erros ou não, e lista os detalhes das linhas
     * inválidas.</p>
     *
     * @param linhasDoArquivo Lista de objetos {@code Linha} que representam as
     * linhas do arquivo a serem validadas.
     * @param relatorioIntegridade Lista de {@code String} onde será registrado
     * o relatório de integridade das linhas.
     * @param detalharVerificacao caso true o método detalhará no
     * relatorioIntegridade todas as validações realizadas mesmo que não
     * encontre erros
     */
    private static void validarLinhas(List<Linha> linhasDoArquivo, List<String> relatorioIntegridade, boolean detalharVerificacao) {

        Iterator<Linha> iterador = linhasDoArquivo.iterator();
        List<Map.Entry<String, Linha>> listaDeErros = new ArrayList<>();

        //Percorre e valida linhas do arquivo, adicionando inconsistências à lista de erros.
        while (iterador.hasNext()) {
            Linha linha = iterador.next();
            String erro = validarLinha(linha);
            if (!erro.isBlank()) {
                listaDeErros.add(Map.entry(erro, linha));
            }
        }
        
        if (!detalharVerificacao && listaDeErros.isEmpty()) {
            return;
        }

        relatorioIntegridade.add("\nCAMPO INVÁLIDO:");
        if (listaDeErros.isEmpty()) {
            relatorioIntegridade.add("\tNenhuma linha com erro");
        } else {
            for (Map.Entry<String, Linha> entry : listaDeErros) {
                relatorioIntegridade.add(String.format("\tLinha %4d - %s - %s", entry.getValue().getEndereco(), entry.getKey(), entry.getValue().getConteudo()));
            }
            relatorioIntegridade.add("\tInválidas: " + listaDeErros.size());
        }

    }

    /**
     * Valida todos os campos de uma linha, retornando uma mensagem de erro
     * formatada se houver algum campo inválido.
     *
     * <p>
     * Este método verifica inicialmente se a quantidade de campos na linha está
     * correta. Se a quantidade estiver incorreta, uma mensagem de erro é
     * retornada. Em seguida, valida cada campo individualmente utilizando o
     * método {@code validarCampo}. Se algum campo for inválido, uma mensagem de
     * erro é construída e retornada; caso contrário, retorna {@code null}.</p>
     *
     * @param linha O objeto {@code Linha} que representa a linha a ser
     * validada.
     * @return Uma mensagem de erro formatada se houver campos inválidos,
     * {@code null} se todos os campos forem válidos.
     */
    private static String validarLinha(Linha linha) {
        String[] campos = linha.getConteudo().split(";");
        if (campos.length != QTD_CAMPOS) {
            return String.format("Qtd campos incorreta. Esperados %d, encontrados %d", QTD_CAMPOS, campos.length);
        } else {
            List<String> errosNaLinha = new ArrayList<>();
            for (int i = 0; i < campos.length; i++) {
                if (i == 0) {
                    errosNaLinha.add(validarCampo(campos[0].substring(0, 10), i));
                } else if (i == 1) {
                    errosNaLinha.add(validarCampo(campos[0].substring(10, 18), i));
                } else {
                    errosNaLinha.add(validarCampo(campos[i], i + 1));
                }
            }

            if (errosNaLinha.isEmpty()) {
                return null;
            } else {
                String msgDeErro = "";
                for (String erro : errosNaLinha) {
                    msgDeErro = msgDeErro.concat(erro);
                }
                return String.format("%s", msgDeErro);
            }
        }
    }

    /**
     * Valida um campo específico de acordo com o índice fornecido, retornando
     * uma mensagem de erro formatada se o campo for inválido.
     *
     * <p>
     * Este método usa expressões regulares para verificar se o campo fornecido
     * corresponde ao padrão esperado com base no índice. Se o campo não for
     * válido, uma mensagem de erro é retornada; caso contrário, uma string
     * vazia é retornada.</p>
     *
     * @param campo O valor do campo a ser validado.
     * @param indice O índice que indica qual campo está sendo validado, onde
     * cada valor corresponde a um campo específico.
     * @return Uma mensagem de erro formatada se o campo for inválido, ou uma
     * string vazia se o campo for válido.
     */
    private static String validarCampo(String campo, int indice) {
        Pattern pattern;
        String regex;
        String msgErro = "";

        switch (indice) {
            case 0: //valida data_timestamp
                regex = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$";
                msgErro = "Data: ";
                break;
            case 1: //valida hora_timestamp
                regex = "^(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
                msgErro = "Hora: ";
                break;
            case 2: //valida config_id
                regex = "^\"([2-9]|1[0-8]).[0-9]{1,5}.([0-9]|1[0-8])\"$";
                msgErro = "config_id: ";
                break;
            //NÃO IMPLEMENTADA
//            case 3: //valida sect_config
//                msgErro = "sect_config: ";
//                break;
            case 4: //valida ctr
                regex = "^\"CTR([0-1][0-9]|2[0-1])\"$";
                msgErro = "CTR: ";
                break;
            case 5: //valida ass
                regex = "^\"(ASS([0-1][0-9]|2[0-1])|)\"$";
                msgErro = "ASS: ";
                break;
            case 6: //valida sector
                regex = "^\"(S0[1-9]{1}|S1[0-8]{1}|S6F|18F)\"$";
                msgErro = "sector: ";
                break;
            case 7: //valida qtd_sectors
                regex = "^([1-9]{1}|1[0-8])$";
                msgErro = "qtd_sector: ";
                break;
            case 8: //valida qtd_ass
                regex = "^([0-9]{1}|1[0-8]{1})$";
                msgErro = "qtd_ass: ";
                break;
            case 9: //valida callsign
                regex = "^([a-zA-Z0-9]{4,7})$";
                msgErro = "callsign: ";
                break;
            case 10: //valida ADEP
                regex = "^([A-Z]{2}[A-Z0-9]{2})$";
                msgErro = "ADEP: ";
                break;
            case 11: //valida ADES
                regex = "^([A-Z]{2}[A-Z0-9]{2})$";
                msgErro = "ADES: ";
                break;
            case 12: //valida DOF
                regex = "^([0-9]{6})$";
                msgErro = "DOF: ";
                break;
            case 13: //valida EOBT
                regex = "^([0-1][0-9]|2[0-3])[0-5][0-9]$";
                msgErro = "EOBT: ";
                break;
            case 14: //valida SSR
                regex = "^A[0-7]{4}$";
                msgErro = "SSR: ";
                break;
            case 15: //valida flrul
                regex = "^(I|V|Y|Z)$";
                msgErro = "flrul: ";
                break;
            default:
                return "";
        }
        pattern = Pattern.compile(regex);
        if (!pattern.matcher(campo).matches()) {
            msgErro = msgErro.concat(campo);
            return "|" + msgErro + "|";
        }
        return "";
    }
}
