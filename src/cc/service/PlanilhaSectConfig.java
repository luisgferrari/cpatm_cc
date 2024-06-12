package cc.service;

import cc.csv.Csv;
import cc.entity.Linha;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * A classe {@code PlanilhaSectConfig} representa um arquivo CSV com registros de
 * configuração de consoles do ACC exportados do SAGITARIO e fornece métodos
 * para verificar a integridade desses dados.
 *
 * <p>
 * Esta classe estende a classe {@code Planilha} e inclui constantes e métodos
 * específicos para a validação de arquivos CSV. O método principal
 * {@code verificarIntegridade} realiza várias verificações de integridade nos
 * dados do arquivo CSV e gera um relatório detalhado.</p>
 *
 * @author luisg
 */
public class PlanilhaSectConfig extends Planilha {

    /**
     * O cabeçalho padrão para uma planilha SectConfig. Este cabecalho define a
     * estrutura esperada dos dados na planilha
     */
    public static final String CABECALHO = "week;day;time;config_id;CTR;ASS;SETOR;QTD_CTR;QTD_ASS;MOV_ATCO;MOV_SET;SECT_CONFIG";
    /**
     * A quantidade de campos esperados em cada linha da planilha SectConfig.
     * Calculada com base na quantidade de elementos separados por ponto e
     * vírgula no cabeçalho
     */
    public static final int CABECALHO_LENGTH = CABECALHO.split(";").length;
    /**
     * O sufixo padrão para o nome do arquivo de planilha SectConfig.
     */
    public static final String SUFIXO_ARQUIVO = "_sect_config.csv";
    /**
     * Array de strings que contém os códigos dos setores disponíveis para
     * configuração. Estes códigos são utilizados para verificar a integridade
     * dos dados da planilha _sect_config.csv.
     */
    public static final String[] SETORES = {"S01", "S02", "S03", "S04", "S05", "S06", "S6F", "S07", "S08", "S09", "S10", "S11", "S12", "S13", "S14", "S15", "S16", "S17", "S18", "18F"};

    /**
     * Gera um relatório de integridade para um arquivo CSV especificado.
     *
     * <p>
     * Este método lê as linhas do arquivo CSV especificado e realiza uma série
     * de verificações de integridade nos dados. As verificações incluem a busca
     * pelo cabeçalho correto, a verificação da quantidade correta de campos em
     * cada linha e a detecção de campos vazios ou inválidos. O relatório é
     * gerado indicando possíveis problemas encontrados e é retornado como uma
     * lista de strings. Além disso, o relatório é escrito em um arquivo de
     * texto com o mesmo nome do arquivo CSV, mas com a extensão ".txt".</p>
     *
     * @param inputFile O caminho do arquivo CSV para o qual o relatório de
     * integridade será gerado.
     * @return Uma lista de strings contendo o relatório de integridade gerado.
     */
    public static List<String> verificarIntegridade(Path inputFile) {
        List<String> relatorioIntegridade = new ArrayList<>();
        relatorioIntegridade.add("RELATÓRIO DE INTEGRIDADE - " + inputFile.getFileName().toString());

        try {
            List<Linha> linhasDoArquivo = Csv.lerLinhas(inputFile);

            if (linhasDoArquivo == null) {
                throw new IOException("O Arquivo" + inputFile.toString() + "não pôde ser lido ou está vazio.");
            }

            localizarCabecalho(linhasDoArquivo, relatorioIntegridade, CABECALHO);
            verificarQuantidadeDeCampos(linhasDoArquivo, relatorioIntegridade, CABECALHO_LENGTH);
            verificarCamposVazios(linhasDoArquivo, relatorioIntegridade);
            verificarHorarios(linhasDoArquivo, relatorioIntegridade);
            verificarQtdDeControladores(linhasDoArquivo, relatorioIntegridade, false);
            verificarQtdDeAssistentes(linhasDoArquivo, relatorioIntegridade, false);

            Path outputFile = Paths.get(inputFile.getParent().toString(), inputFile.getFileName().toString().replace(".csv", ".txt"));
            Csv.escrever(relatorioIntegridade, outputFile);

        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + inputFile.toString());
            System.err.println(Arrays.toString(e.getStackTrace()));

            relatorioIntegridade.add("\nErro ao ler arquivo: " + inputFile.toString());
            relatorioIntegridade.add(Arrays.toString(e.getStackTrace()));

            Path outputFile = Paths.get(inputFile.getParent().toString(), inputFile.getFileName().toString().replace(".csv", "-ERRO.txt"));
            Csv.escrever(relatorioIntegridade, outputFile);

            return relatorioIntegridade;
        }

//        for (String string : relatorioIntegridade) {
//            System.out.println(string);
//        }
        return relatorioIntegridade;
    }

    /**
     * Verifica a consistência entre a quantidade de controladores (qtd_CTR) e o
     * identificador de configuração (config_id) para cada linha do arquivo.
     * Adiciona um relatório de integridade contendo as linhas inconsistentes ou
     * uma mensagem informando que nenhuma inconsistência foi encontrada.
     * Opcionalmente, remove as linhas inconsistentes.
     *
     * @param linhasDoArquivo uma lista de objetos {@link Linha} representando
     * as linhas do arquivo a serem verificadas
     * @param relatorioIntegridade uma lista de {@link String} onde o relatório
     * de integridade será adicionado
     * @param removerInconsistencias se verdadeiro, remove as linhas
     * inconsistentes da lista de linhas do arquivo
     */
    private static void verificarQtdDeControladores(List<Linha> linhasDoArquivo, List<String> relatorioIntegridade, Boolean removerInconsistencias) {
        relatorioIntegridade.add("\nCONDIÇÃO: QTD_CTR INCOMPATÍVEL COM CONFIG_ID");
        Iterator<Linha> iteradorLinhas = linhasDoArquivo.iterator();
        List<Linha> linhasComErro = new ArrayList<>();

        while (iteradorLinhas.hasNext()) {
            Linha linha = iteradorLinhas.next();
            String[] campos = linha.getConteudo().split(";");
            String qtd_CTR = campos[7];
            String config_id = campos[3];
            String config_id_CTR = config_id.split("\\.")[0].replace("\"", "");
            if (!config_id_CTR.equals(qtd_CTR)) {
                linhasComErro.add(linha);
                relatorioIntegridade.add("\tLinha " + String.format("%4d", linha.getEndereco()) + " - " + linha.getConteudo());
                if (removerInconsistencias) {
                    iteradorLinhas.remove();
                }
            }
        }

        if (linhasComErro.isEmpty()) {
            relatorioIntegridade.add("\tNenhuma linha com erro");
        }
    }

    /**
     * Verifica a consistência entre a quantidade de assistentes (qtd_ASS) e o
     * identificador de configuração (config_id) para cada linha do arquivo.
     * Adiciona um relatório de integridade contendo as linhas inconsistentes ou
     * uma mensagem informando que nenhuma inconsistência foi encontrada.
     * Opcionalmente, remove as linhas inconsistentes.
     *
     * @param linhasDoArquivo uma lista de objetos {@link Linha} representando
     * as linhas do arquivo a serem verificadas
     * @param relatorioIntegridade uma lista de {@link String} onde o relatório
     * de integridade será adicionado
     * @param removerInconsistencias se verdadeiro, remove as linhas
     * inconsistentes da lista de linhas do arquivo
     */
    private static void verificarQtdDeAssistentes(List<Linha> linhasDoArquivo, List<String> relatorioIntegridade, Boolean removerInconsistencias) {
        relatorioIntegridade.add("\nCONDIÇÃO: QTD_ASS INCOMPATÍVEL COM CONFIG_ID");
        Iterator<Linha> iteradorLinhas = linhasDoArquivo.iterator();
        List<Linha> linhasComErro = new ArrayList<>();

        while (iteradorLinhas.hasNext()) {
            Linha linha = iteradorLinhas.next();
            String[] campos = linha.getConteudo().split(";");
            String qtd_ASS = campos[8];
            String config_id = campos[3];
            String config_id_CTR = config_id.split("\\.")[2].replace("\"", "");
            if (!config_id_CTR.equals(qtd_ASS)) {
                linhasComErro.add(linha);
                relatorioIntegridade.add("\tLinha " + String.format("%4d", linha.getEndereco()) + " - " + linha.getConteudo());
                if (removerInconsistencias) {
                    iteradorLinhas.remove();
                }
            }
        }

        if (linhasComErro.isEmpty()) {
            relatorioIntegridade.add("\tNenhuma linha com erro");
        }
    }

    /**
     * Verifica os horários das linhas fornecidas para garantir a integridade
     * dos dados.
     *
     * Este método executa três principais verificações: 1. Mapeia as linhas
     * para horários específicos utilizando o método `mapearMinutos`. 2.
     * Verifica se há horários ausentes utilizando o método `verificarAusentes`.
     * 3. Verifica se há horários com excesso de linhas utilizando o método
     * `verificarHorarioComExcesso`.
     *
     * @param linhas Uma lista de objetos `Linha` que precisam ser verificados.
     * @param relatorioIntegridade Uma lista de strings que será utilizada para
     * armazenar o relatório de integridade.
     *
     * O método começa mapeando as linhas para horários específicos, armazenando
     * os resultados em um mapa. Em seguida, verifica se há horários ausentes e
     * horários com excesso de linhas, adicionando quaisquer problemas
     * encontrados ao `relatorioIntegridade`.
     */
    private static void verificarHorarios(List<Linha> linhas, List<String> relatorioIntegridade) {
        Map<LocalTime, List<Linha>> mapaMinutos = mapearMinutos(linhas);
        verificarAusentes(mapaMinutos, relatorioIntegridade);
        verificarHorarioComExcesso(mapaMinutos, relatorioIntegridade);
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
            mapaMinutos.computeIfAbsent(minuto, listaExistente -> new ArrayList<>()).add(linha);
        }

        popularMinutosFaltantes(mapaMinutos);

        return mapaMinutos;
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
        Set<Entry<LocalTime, List<Linha>>> entrySet = mapaMinutos.entrySet();

        for (Entry<LocalTime, List<Linha>> entry : entrySet) {
            if (entry.getValue().isEmpty() || entry.getValue() == null) {
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
     * Analisa o mapa `mapaMinutos` para identificar entradas (chaves
     * `LocalTime`) com uma correspondente `List<Linha>` que excede o tamanho da
     * constante `SETORES`. Isso indica uma condição de erro onde há mais linhas
     * registradas do que setores disponíveis para um horário específico.
     *
     * @param mapaMinutos Um `Map` contendo entradas onde a chave é um
     * `LocalTime` representando um horário e o valor é uma `List<Linha>`
     * contendo objetos `Linha`.
     * @param relatorioIntegridade Uma `List<String>` usada para armazenar o
     * relatório de integridade.
     *
     * Este método itera por cada entrada no mapa `mapaMinutos`. Para cada
     * entrada, verifica se a `List<Linha>` associada não é nula e não está
     * vazia. Se o tamanho da lista exceder o comprimento da constante
     * `SETORES`, isso indica uma condição de erro.
     *
     * O método cria uma `listaExcessos` temporária (`List<String>`) para
     * armazenar detalhes sobre os erros identificados. Se um erro for
     * encontrado para um horário específico, o próprio horário e detalhes para
     * cada objeto `Linha` excedente são adicionados à `listaExcessos`.
     *
     * Finalmente, o método verifica se `listaExcessos` está vazia. Se estiver
     * vazia, adiciona uma mensagem indicando "Nenhum horário com erro" ao
     * `relatorioIntegridade`. Caso contrário, adiciona todos os elementos de
     * `listaExcessos` ao `relatorioIntegridade`, populando efetivamente o
     * relatório com detalhes sobre os horários e excessos de linhas
     * identificados.
     */
    private static void verificarHorarioComExcesso(Map<LocalTime, List<Linha>> mapaMinutos, List<String> relatorioIntegridade) {
        relatorioIntegridade.add("\nCONDIÇÃO: HORÁRIO COM EXCESSO DE REGISTROS");
        List<String> listaExcessos = new ArrayList<>();
        Set<Entry<LocalTime, List<Linha>>> entrySet = mapaMinutos.entrySet();

        for (Entry<LocalTime, List<Linha>> entry : entrySet) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (entry.getValue().size() > SETORES.length) {
                    listaExcessos.add(String.format("\t%s", entry.getKey().toString()));
                    List<Linha> listaLinhas = entry.getValue();
                    for (Linha linha : listaLinhas) {
                        listaExcessos.add(String.format("\tLinha %4d - %s", linha.getEndereco(), linha.getConteudo()));
                    }
                }
            }
        }

        if (!listaExcessos.isEmpty()) {
            relatorioIntegridade.addAll(listaExcessos);
        } else {
            relatorioIntegridade.add("\tNenhum horário com erro");
        }
    }
}
