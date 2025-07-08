package org.example.model;

public enum StatusArquivo {
    
    PRONTO("Pronto"),
    VALIDADO("Validado"),
    ERRO("Erro"),
    TIPO_DESCONHECIDO("Tipo desconhecido");

    private final String label;

    private StatusArquivo(String label){
        this.label = label;
    }

    @Override
    public String toString(){
        return label;
    }
}
