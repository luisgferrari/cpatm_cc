package org.example.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.example.csv.Csv;
import org.example.model.Linha;
import org.example.util.LoggerUtil;

/**
 * A classe {@code PlanilhaConfig} representa um arquivo CSV com registros de
 * quantidade de controladores e assistentes utilizados assim como número de
 * aeronaves na FIR CW exportados pelo SAGITARIO e fornece métodos para
 * verificar a integridade desses dados.
 *
 * <p>
 * Esta classe estende a classe {@code Planilha} e inclui constantes e métodos
 * específicos para a validação de arquivos CSV. O método principal
 * {@code verificarIntegridade} realiza várias verificações de integridade nos
 * dados do arquivo CSV e gera um relatório detalhado.</p>
 *
 * @author luisg
 */
public class PlanilhaConfig extends Planilha {


    private static final Logger log = LoggerUtil.getLogger();
    /**
     * O cabeçalho padrão para uma planilha Config. Este cabecalho define a
     * estrutura esperada dos dados na planilha.
     */
    public static final String CABECALHO = "week;day;time;config_id;QTD_CTR;QTD_ASS;MOV;SECT_CONFIG";
    /**
     * A quantidade de campos esperados em cada linha da planilha Config.
     * Calculada com base na quantidade de elementos separados por ponto e
     * vírgula no cabeçalho.
     */
    public static final int QTD_CAMPOS = CABECALHO.split(";").length;
    /**
     * A quantidade de linhas esperadas na planilha. Por padrão, cada arquivo
     * contém 1 linha por minuto ao longo de 24 horas, totalizando assim 1440
     * linhas.
     */
    public static final int QTD_LINHAS = 1440;
    /**
     * O sufixo padrão para o nome do arquivo de planilha Config.
     */
    public static final String SUFIXO = "_config.csv";

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
    public static boolean verificarIntegridade(Path inputFile, boolean detalharVerificacao) {
        log.info("Verificando planilha config: " + inputFile);

        List<String> relatorioIntegridade = new ArrayList<>();
        List<Linha> linhasDoArquivo;
        Path outputFile = Paths.get(inputFile.getParent().toString().concat("\\Relatórios"), inputFile.getFileName().toString().replace(".csv", ".txt"));
        String inputFileName = inputFile.getFileName().toString();
        
        try {
            linhasDoArquivo = Csv.getLines(inputFile);
        } catch (IOException e) {
            String msgErro = "Exceção ao ler arquivo " + inputFileName;
            registrarErro(inputFile, msgErro, e);
            return false;
        }

        try {
            localizarCabecalho(linhasDoArquivo, relatorioIntegridade, CABECALHO, detalharVerificacao);
            verificarQuantidadeDeCampos(linhasDoArquivo, relatorioIntegridade, QTD_CAMPOS, detalharVerificacao);
            verificarCamposVazios(linhasDoArquivo, relatorioIntegridade, detalharVerificacao);
            contarQtdLinhas(linhasDoArquivo, relatorioIntegridade, detalharVerificacao);
            verificarHorarios(linhasDoArquivo, relatorioIntegridade, detalharVerificacao);
            
        } catch (Exception e) {
            String msgErro = "Exceção ao processar arquivo " + inputFileName;
            registrarErro(inputFile, msgErro, e);
            return false;
        }

        if (relatorioIntegridade.isEmpty()) {
            relatorioIntegridade.add("OK");
        }
        relatorioIntegridade.add(0,"RELATÓRIO DE INTEGRIDADE");
        relatorioIntegridade.add(1, inputFileName);

        try {
            Csv.escrever(relatorioIntegridade, outputFile);
        } catch (Exception e) {
            String msgErro = "Erro ao escrever o relatório de integridade para o arquivo: " + inputFileName;
            registrarErro(inputFile, msgErro, e);
            return false;
        }
        
        return true;
    }

    /**
     * Conta a quantidade de linhas na lista de linhas fornecida e atualiza o
     * relatório de integridade com os resultados da contagem.
     *
     * <p>
     * Este método compara a quantidade de linhas encontradas na lista com a
     * quantidade esperada (definida pela constante {@code QTD_LINHAS}). Se as
     * quantidades coincidirem, o relatório indicará que o resultado está OK.
     * Caso contrário, indicará falha e a diferença será adicionada ao
     * relatório.</p>
     *
     * @param linhas a lista de linhas a ser verificada
     * @param relatorioIntegridade a lista onde o relatório de integridade será
     * adicionado
     * @param detalharVerificacao caso true o método detalhará no
     * relatorioIntegridade todas as validações realizadas mesmo que não
     * encontre erros
     */
    private static void contarQtdLinhas(List<Linha> linhas, List<String> relatorioIntegridade, boolean detalharVerificacao) {
        int qtdLinhas = linhas.size();

        if (QTD_LINHAS == qtdLinhas) {
            if (detalharVerificacao) {
                relatorioIntegridade.add("\nQUANTIDADE DE LINHAS");
                relatorioIntegridade.add("\tResultado: OK");
            }
        } else {
            relatorioIntegridade.add("\nQUANTIDADE DE LINHAS");
            relatorioIntegridade.add("\tQtd esperada: " + QTD_LINHAS + " linhas");
            relatorioIntegridade.add("\tQtd encontrada: " + qtdLinhas + " linhas");
            int diferenca = qtdLinhas - QTD_LINHAS;
            if (diferenca > 0) {
                relatorioIntegridade.add(String.format("\tDiferença: %d linhas a mais", diferenca));
            } else {
                relatorioIntegridade.add(String.format("\tDiferença: %d linhas a menos", Math.abs(diferenca)));
            }
        }
    }

    /**
     * Verifica a integridade dos horários na lista de linhas fornecida.
     * <p>
     * Este método mapeia as linhas para seus respectivos minutos, verifica
     * horários ausentes e duplicados, e adiciona os resultados ao relatório de
     * integridade.
     * </p>
     *
     * @param linhas a lista de objetos Linha a serem verificadas
     * @param relatorioIntegridade a lista onde será adicionado o relatório de
     * integridade
     * @param detalharVerificacao caso true o método detalhará no
     * relatorioIntegridade todas as validações realizadas mesmo que não
     * encontre erros
     */
    private static void verificarHorarios(List<Linha> linhas, List<String> relatorioIntegridade, boolean detalharVerificacao) {
        Map<LocalTime, List<Linha>> mapaMinutos = mapearMinutos(linhas);
        verificarAusentes(mapaMinutos, relatorioIntegridade, detalharVerificacao);
        verificarDuplicados(mapaMinutos, relatorioIntegridade, detalharVerificacao);
    }

    /**
     * Mapeia uma lista de objetos do tipo Linha em um mapa que associa cada
     * minuto do dia a uma lista de Linhas.
     * <p>
     * Para cada objeto Linha na lista fornecida, este método extrai o tempo (no
     * formato HH:mm:ss) do conteúdo da Linha, agrupa as linhas pelo minuto
     * exato e garante que cada minuto do dia esteja representado no mapa, mesmo
     * que não haja linhas correspondentes a alguns minutos.
     * </p>
     *
     * @param linhas a lista de objetos Linha a serem mapeados
     * @return um mapa onde a chave é um LocalTime representando um minuto do
     * dia e o valor é uma lista de objetos Linha correspondentes a esse minuto
     */
    private static Map<LocalTime, List<Linha>> mapearMinutos(List<Linha> linhas) {
        Map<LocalTime, List<Linha>> mapaMinutos = new TreeMap<>();

        for (Linha linha : linhas) {
            LocalTime minuto = LocalTime.parse(linha.getConteudo().split(";")[2], DateTimeFormatter.ISO_LOCAL_TIME);
            List<Linha> novaLista = mapaMinutos.compute(minuto, (key, listaExistente) -> {
                if (listaExistente == null) {
                    listaExistente = new ArrayList<>();
                }
                listaExistente.add(linha);
                return listaExistente;
            });
            mapaMinutos.put(minuto, novaLista);
        }

        popularMinutosFaltantes(mapaMinutos);

        return mapaMinutos;
    }

    /**
     * Verifica e registra horários ausentes no relatório de integridade.
     * <p>
     * Este método percorre o mapa de minutos e verifica se há minutos sem
     * linhas associadas. Se encontrar minutos ausentes, adiciona-os ao
     * relatório de integridade.
     * </p>
     *
     * @param mapaMinutos o mapa contendo os minutos e suas respectivas listas
     * de linhas
     * @param relatorioIntegridade a lista onde será adicionado o relatório de
     * integridade
     * @param detalharVerificacao caso true o método detalhará no
     * relatorioIntegridade todas as validações realizadas mesmo que não
     * encontre erros
     */
    private static void verificarAusentes(Map<LocalTime, List<Linha>> mapaMinutos, List<String> relatorioIntegridade, boolean detalharVerificacao) {
        List<String> listaAusentes = new ArrayList<>();
        Set<Map.Entry<LocalTime, List<Linha>>> entrySet = mapaMinutos.entrySet();

        for (Map.Entry<LocalTime, List<Linha>> entry : entrySet) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                listaAusentes.add(String.format("\t%s", entry.getKey().toString()));
            }
        }

        if (!listaAusentes.isEmpty()) {
            relatorioIntegridade.add("\nHORÁRIO AUSENTE");
            relatorioIntegridade.addAll(listaAusentes);
            relatorioIntegridade.add(String.format("\tQuantidade: %d", listaAusentes.size()));
        } else {
            if (detalharVerificacao) {
                relatorioIntegridade.add("\nHORÁRIO AUSENTE");
                relatorioIntegridade.add("\tNenhum horário ausente");
            }
        }
    }

    /**
     * Verifica e registra horários duplicados no relatório de integridade.
     * <p>
     * Este método percorre o mapa de minutos e verifica se há minutos com
     * múltiplas linhas associadas. Se encontrar horários duplicados,
     * adiciona-os ao relatório de integridade.
     * </p>
     *
     * @param mapaMinutos o mapa contendo os minutos e suas respectivas listas
     * de linhas
     * @param relatorioIntegridade a lista onde será adicionado o relatório de
     * integridade
     * @param detalharVerificacao caso true o método detalhará no
     * relatorioIntegridade todas as validações realizadas mesmo que não
     * encontre erros
     */
    private static void verificarDuplicados(Map<LocalTime, List<Linha>> mapaMinutos, List<String> relatorioIntegridade, boolean detalharVerificacao) {
        List<String> listaDuplicados = getListaDuplicados(mapaMinutos);

        if (!listaDuplicados.isEmpty()) {
            relatorioIntegridade.add("\nHORÁRIO DUPLICADO");
            relatorioIntegridade.addAll(listaDuplicados);
        } else {
            if (detalharVerificacao) {
                relatorioIntegridade.add("\nHORÁRIO DUPLICADO");
                relatorioIntegridade.add("\tNenhum horário duplicado");
            }
        }
    }

    private static List<String> getListaDuplicados(Map<LocalTime, List<Linha>> mapaMinutos) {
        List<String> listaDuplicados = new ArrayList<>();
        Set<Map.Entry<LocalTime, List<Linha>>> entrySet = mapaMinutos.entrySet();

        for (Map.Entry<LocalTime, List<Linha>> entry : entrySet) {
            if (entry.getValue().size() > 1) {
                for (Linha linha : entry.getValue()) {
                    listaDuplicados.add(String.format("\t%s - Linha %4d - %s", entry.getKey().toString(), linha.getEndereco(), linha.getConteudo()));
                }
                listaDuplicados.add("");
            }
        }
        return listaDuplicados;
    }

    /**
     * Garante que todos os minutos do dia estejam presentes no mapa, mesmo que
     * não haja linhas correspondentes.
     *
     * @param mapaMinutos o mapa a ser preenchido com todos os minutos do dia
     */
    private static void popularMinutosFaltantes(Map<LocalTime, List<Linha>> mapaMinutos) {
        LocalTime min = LocalTime.MIN;
        do {
            mapaMinutos.computeIfAbsent(min, k -> new ArrayList<>());
            min = min.plusMinutes(1);
        } while (min.isAfter(LocalTime.MIDNIGHT));
    }

}
