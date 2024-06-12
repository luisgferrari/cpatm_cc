package cc.service;

import cc.entity.Linha;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author luisg
 */
class Planilha {

    /**
     * Localiza e remove as linhas que correspondem ao cabeçalho esperado do
     * arquivo CSV.
     *
     * <p>
     * Este método procura pela linha de cabeçalho definida pela constante
     * {@code #CABECALHO}. Se encontrada, a linha de cabeçalho é adicionada à
     * lista de cabeçalhos e removida da lista original de linhas. Um relatório
     * de integridade é atualizado com as informações sobre a presença e
     * localização do cabeçalho.</p>
     *
     * @param linhas a lista de linhas do arquivo CSV a ser processada.
     * @param relatorioIntegridade a lista onde os resultados da verificação de
     * integridade serão adicionados.
     * @param cabecalho a string contendo o cabecalho esperado no arquivo.
     */
    protected static void localizarCabecalho(List<Linha> linhas, List<String> relatorioIntegridade, String cabecalho) {
        List<Linha> listaCabecalhos = new ArrayList<>();
        relatorioIntegridade.add("\nEXCLUSÃO: CABEÇALHO");

        Iterator<Linha> iteradorLinhas = linhas.iterator();
        while (iteradorLinhas.hasNext()) {
            Linha linha = iteradorLinhas.next();
            if (linha.getConteudo().equals(cabecalho)) {
                listaCabecalhos.add(linha);
                iteradorLinhas.remove();
            }
        }

        if (!listaCabecalhos.isEmpty()) {
            for (Linha linha : listaCabecalhos) {
                relatorioIntegridade.add("\tLinha " + String.format("%4d - %s", linha.getEndereco(), linha.getConteudo()));
            }
        } else {
            relatorioIntegridade.add("\tCabeçalho não encontrado");
        }
    }

    /**
     * Verifica se a quantidade de campos de cada linha corresponde à quantidade
     * de campos do cabeçalho.
     *
     * <p>
     * Este método percorre cada linha da lista de linhas e verifica se a
     * quantidade de campos, delimitados por ponto e vírgula (;), corresponde ao
     * esperado conforme definido no cabeçalho. Linhas com quantidade de campos
     * diferente são adicionadas a uma lista de erro e removidas da lista
     * original. Um relatório de integridade é atualizado com as informações
     * sobre essas linhas.</p>
     *
     * @param linhas a lista de linhas do arquivo CSV a ser processada
     * @param relatorioIntegridade a lista onde os resultados da verificação de
     * integridade serão adicionados
     * @param qtdEsperadaDeCampos integer com a quantidade esperada de campos
     * por linha
     * @return lista contendo as linhas onde algum erro foi identificado
     */
    protected static List<Linha> verificarQuantidadeDeCampos(List<Linha> linhas, List<String> relatorioIntegridade, Integer qtdEsperadaDeCampos) {
        relatorioIntegridade.add("\nEXCLUSÃO: QUANTIDADE DE CAMPOS INCOMPATÍVEL COM O CABEÇALHO");

        Iterator<Linha> iteradorLinhas = linhas.iterator();
        List<Linha> linhasComErro = new ArrayList<>();

        while (iteradorLinhas.hasNext()) {
            Linha linha = iteradorLinhas.next();
            if (linha.getConteudo().split(";").length != qtdEsperadaDeCampos) {
                linhasComErro.add(linha);
                relatorioIntegridade.add("\tLinha " + String.format("%4d - %s", linha.getEndereco(), linha.getConteudo()));
                iteradorLinhas.remove();
            }
        }

        if (linhasComErro.isEmpty()) {
            relatorioIntegridade.add("\tNenhuma linha filtrada");
        } else {
            relatorioIntegridade.add("\tFiltradas: " + linhasComErro.size());
        }

        return linhasComErro;
    }

    /**
     * Verifica se alguma linha possui campos vazios e as exclui do
     * processamento
     *
     * <p>
     * Este método percorre cada linha da lista de linhas e verifica se há algum
     * campo vazio. Linhas contendo campos vazios são adicionadas a uma lista de
     * erro e removidas da lista original. Um relatório de integridade é
     * atualizado com as informações sobre as linhas com campos vazios</p>
     *
     * @param linhas a lista de linhas do arquivo CSV a ser processada
     * @param relatorioIntegridade a lista onde os resultados da verificação de
     * integridade serão adicionados
     */
    protected static void verificarCamposVazios(List<Linha> linhas, List<String> relatorioIntegridade) {
        relatorioIntegridade.add("\nEXCLUSÃO: LINHA COM CAMPO VAZIO");
        Iterator<Linha> iteradorLinhas = linhas.iterator();
        List<Linha> linhasComErro = new ArrayList<>();

        while (iteradorLinhas.hasNext()) {
            Linha linha = iteradorLinhas.next();
            if (linhaTemCampoVazio(linha.getConteudo())) {
                linhasComErro.add(linha);
                relatorioIntegridade.add("\tLinha " + String.format("%4d", linha.getEndereco()) + " - " + linha.getConteudo());
                iteradorLinhas.remove();
            }
        }

        if (linhasComErro.isEmpty()) {
            relatorioIntegridade.add("\tNenhuma linha filtrada");
        } else {
            relatorioIntegridade.add("\tFiltradas: " + linhasComErro.size());
        }
    }

    /**
     * Verifica se uma linha possui algum campo vazio
     *
     * <p>
     * Este método recebe uma string de conteúdo de linha e verifica se algum
     * dos campos da linha está vazio. Um campo é considerado vazio se consistir
     * apenas de espaços em branco ou se for uma string vazia. Retorna
     * verdadeiro se um campo vazio for encontrado e falso caso contrário.</p>
     *
     * @param linha a string contendo o conteúdo da linha a ser verificado
     * @return true se a linha contiver um campo vazio, false caso contrário
     */
    protected static boolean linhaTemCampoVazio(String linha) {
        String[] campos = linha.split(";");
        for (String campo : campos) {
            if (campo.isBlank()) {
                return true;
            }
        }
        return false;
    }
}
