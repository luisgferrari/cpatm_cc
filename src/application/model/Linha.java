package application.model;

/**
 * Representa uma linha em um arquivo CSV, contendo seu número de endereço e o
 * conteúdo da linha. O número de endereço indica a posição da linha no arquivo.
 *
 * @author luisg
 */
public class Linha {

    private final Integer endereco;
    private final String conteudo;

    /**
     * Cria uma nova instância de Linha com o número de endereço e conteúdo
     * especificados.
     *
     * @param endereco o número de endereço da linha
     * @param conteudo o conteúdo da linha
     */
    public Linha(Integer endereco, String conteudo) {
        this.endereco = endereco;
        this.conteudo = conteudo;
    }

    /**
     * Obtém o número de endereço da linha.
     *
     * @return o número de endereço da linha
     */
    public Integer getEndereco() {
        return endereco;
    }

    /**
     * Obtém o conteúdo da linha.
     *
     * @return o conteúdo da linha
     */
    public String getConteudo() {
        return conteudo;
    }

}
