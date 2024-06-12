package cc.service;

import cc.csv.Csv;
import cc.entity.Linha;
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
    public static final String SUFIXO_ARQUIVO = "_config.csv";

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
     * @return uma lista de strings contendo o relatório de integridade
     */
    public static List<String> verificarIntegridade(Path inputFile) {
        List<String> relatorioIntegridade = new ArrayList<>();
        relatorioIntegridade.add("RELATÓRIO DE INTEGRIDADE - " + inputFile.getFileName().toString());

        try {
            List<Linha> linhasDoArquivo = Csv.lerLinhas(inputFile);

            localizarCabecalho(linhasDoArquivo, relatorioIntegridade, CABECALHO);
            verificarQuantidadeDeCampos(linhasDoArquivo, relatorioIntegridade, QTD_CAMPOS);
            verificarCamposVazios(linhasDoArquivo, relatorioIntegridade);
            
            contarQtdLihas(linhasDoArquivo, relatorioIntegridade);
            verificarHorarios(linhasDoArquivo, relatorioIntegridade);

            Path outputFile = Paths.get(inputFile.getParent().toString(), inputFile.getFileName().toString().replace(".csv", ".txt"));
            Csv.escrever(relatorioIntegridade, outputFile);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            Path outputFile = Paths.get(inputFile.getParent().toString(), inputFile.getFileName().toString().replace(".csv", "-ERRO.txt"));
            relatorioIntegridade.add(e.getLocalizedMessage());
            Csv.escrever(relatorioIntegridade, outputFile);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            Path outputFile = Paths.get(inputFile.getParent().toString(), inputFile.getFileName().toString().replace(".csv", "-ERRO.txt"));
            relatorioIntegridade.add(e.getLocalizedMessage());
            Csv.escrever(relatorioIntegridade, outputFile);
        }

        return relatorioIntegridade;
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
     */
    private static void contarQtdLihas(List<Linha> linhas, List<String> relatorioIntegridade) {
        relatorioIntegridade.add("\nCONDIÇÃO: QUANTIDADE DE LINHAS");
        int qtdLinhas = linhas.size();

        if (QTD_LINHAS == qtdLinhas) {
            relatorioIntegridade.add("\tResultado: OK");
        } else {
            relatorioIntegridade.add("\tResultado: FALHOU");
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
     */
    private static void verificarHorarios(List<Linha> linhas, List<String> relatorioIntegridade) {
        Map<LocalTime, List<Linha>> mapaMinutos = mapearMinutos(linhas);
        verificarAusentes(mapaMinutos, relatorioIntegridade);
        verificarDuplicados(mapaMinutos, relatorioIntegridade);
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
     */
    private static void verificarAusentes(Map<LocalTime, List<Linha>> mapaMinutos, List<String> relatorioIntegridade) {
        relatorioIntegridade.add("\nCONDIÇÃO: HORÁRIO AUSENTE");
        List<String> listaAusentes = new ArrayList<>();
        Set<Map.Entry<LocalTime, List<Linha>>> entrySet = mapaMinutos.entrySet();

        for (Map.Entry<LocalTime, List<Linha>> entry : entrySet) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                listaAusentes.add(String.format("\t%s", entry.getKey().toString()));
            }
        }

        if (!listaAusentes.isEmpty()) {
            relatorioIntegridade.addAll(listaAusentes);
            relatorioIntegridade.add(String.format("\tQuantidade: %d", listaAusentes.size()));
        } else {
            relatorioIntegridade.add("\tNenhum horário ausente");
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
     */
    private static void verificarDuplicados(Map<LocalTime, List<Linha>> mapaMinutos, List<String> relatorioIntegridade) {
        relatorioIntegridade.add("\nCONDIÇÃO: HORÁRIO DUPLICADO");
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

        if (!listaDuplicados.isEmpty()) {
            relatorioIntegridade.addAll(listaDuplicados);
        } else {
            relatorioIntegridade.add("\tNenhum horário duplicado");
        }
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
            mapaMinutos.computeIfAbsent(min, k -> {
                return new ArrayList<>();
            });
            min = min.plusMinutes(1);
        } while (min.isAfter(LocalTime.MIDNIGHT));
    }

}
