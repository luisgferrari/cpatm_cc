package cc.entity;

/**
 *
 * @author luisg
 */
public class Linha {
    private final Integer endereco;
    private final String conteudo;

    public Linha(Integer endereco, String conteudo) {
        this.endereco = endereco;
        this.conteudo = conteudo;
    }

    public Integer getEndereco() {
        return endereco;
    }

    public String getConteudo() {
        return conteudo;
    }
    
}
