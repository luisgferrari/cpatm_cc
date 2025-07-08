package org.example.model;

public enum TipoArquivo {
    
    FLIGHTS("flights"),
    CONFIG("config"),
    SECT_CONFIG("sect_config"),
    DESCONHECIDO("desconhecido");

    private final String label;

    TipoArquivo(String label){
        this.label = label;
    }

    @Override
    public String toString(){
        return label;
    }
}
