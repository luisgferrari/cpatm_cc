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
     * lista de cabeçalhos e removida da lista original de linhas. O relatório
     * de integridade é atualizado com as informações sobre a presença e
     * localização do cabeçalho.</p>
     *
     * @param conteudo lista de Linhas do arquivo CSV a ser processada.
     * @param relatorio relatório com os resultados das validações.
     * @param cabecalho a string contendo o cabecalho esperado no arquivo.
     * @param detalhar define se o resultado do método é acrescentado ao
     * relatório quando o cabecalho não é encontrado.
     */
    protected static void localizarCabecalho(List<Linha> conteudo, List<String> relatorio, String cabecalho, boolean detalhar) {
        Iterator<Linha> it_conteudo = conteudo.iterator();
        boolean encontrouCabecalho = false;
        List<String> resultado = new ArrayList<>();

        while (it_conteudo.hasNext()) {
            Linha linha = it_conteudo.next();
            if (linha.getConteudo().equals(cabecalho)) {
                resultado.add("\tLinha " + String.format("%4d - %s", linha.getEndereco(), linha.getConteudo()));
                it_conteudo.remove();
                encontrouCabecalho = true;
            }
        }

        if (!detalhar && !encontrouCabecalho) {
            return;
        }

        if (resultado.isEmpty()) {
            resultado.add("\tCabeçalho não encontrado");
        }
        
        resultado.add(0, "\nCABEÇALHO");
        relatorio.addAll(resultado);
    }

    /**
     * Verifica se a quantidade de campos de cada linha corresponde à quantidade
     * de campos do cabeçalho.
     *
     * <p>
     * Este método percorre cada linha da lista de linhas e verifica se a
     * quantidade de campos, delimitados por ponto e vírgula (;), corresponde ao
     * esperado conforme definido no cabeçalho. Para cada linha discrepante uma
     * mensagem é adicionada ao resultado e a linha é removida da lista de
     * conteúdo. O resultado do método é adicionado ao relatório se houver
     * discrepancias e/ou caso a variável detalhar é true.</p>
     *
     * @param conteudo lista de linhas do arquivo CSV a ser processada
     * @param relatorio relatório com os resultados das validações.
     * @param qtdEsperadaDeCampos integer com a quantidade esperada de campos
     * por linha
     * @param detalhar define se o resultado do método é acrescentado ao
     * relatório quando não existem linhas com quantidade de campos
     * incompatíveis.
     * @return lista contendo as linhas onde algum erro foi identificado
     */
    protected static void verificarQuantidadeDeCampos(List<Linha> conteudo, List<String> relatorio, Integer qtdEsperadaDeCampos, boolean detalhar) {
        List<String> resultado = new ArrayList<>();
        Iterator<Linha> it_conteudo = conteudo.iterator();
        boolean encontrouErro = false;

        while (it_conteudo.hasNext()) {
            Linha linha = it_conteudo.next();
            if (linha.getConteudo().split(";").length != qtdEsperadaDeCampos) {
                resultado.add("\tLinha " + String.format("%4d - %s", linha.getEndereco(), linha.getConteudo()));
                it_conteudo.remove();
                encontrouErro = true;
            }
        }

        if (!detalhar && !encontrouErro) {
            return;
        }

        if (resultado.isEmpty()) {
            resultado.add("\tNenhuma linha filtrada");
        } else {
            resultado.add("\tQtd linhas filtradas: " + resultado.size());
        }
        
        resultado.add(0, "\nQUANTIDADE DE CAMPOS INCOMPATÍVEL COM O CABEÇALHO");
        relatorio.addAll(resultado);
    }

    /**
     * Localiza e exclui do processamento linhas que possuem quantidade de
     * campos diferente da quantidade esperada de campos.
     *
     * <p>
     * Este método percorre cada linha do conteúdo e verifica se há algum campos
     * vazios nas linhas. Linhas contendo campos vazios removidas da lista
     * original. O resultado é adicionado ao relatório caso haja linhas
     * excluídas ou caso o parâmetro detalhar seja true.</p>
     *
     * @param conteudo conteúdo do arquivo CSV a ser processado
     * @param relatorio lista onde os resultados da verificação de integridade
     * serão adicionados
     * @param detalhar caso true o método detalhará no relatório
     * todas as validações realizadas mesmo que não encontre erros
     */
    protected static void verificarCamposVazios(List<Linha> conteudo, List<String> relatorio, boolean detalhar) {
        List<String> resultado = new ArrayList<>();
        Iterator<Linha> it_conteudo = conteudo.iterator();
        boolean encontrouCampoVazio = false;

        while (it_conteudo.hasNext()) {
            Linha linha = it_conteudo.next();
            if (linhaTemCampoVazio(linha.getConteudo())) {
                resultado.add("\tLinha " + String.format("%4d", linha.getEndereco()) + " - " + linha.getConteudo());
                it_conteudo.remove();
                encontrouCampoVazio = true;
            }
        }

        if (!detalhar && !encontrouCampoVazio) {
            return;
        }

        if (resultado.isEmpty()) {
            resultado.add("\tNenhuma linha filtrada");
        } else {
            resultado.add("\tFiltradas: " + resultado.size());
        }
        
        resultado.add(0, "\nLINHA COM CAMPO VAZIO");
        relatorio.addAll(resultado);
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
            if (campo == null || campo.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
