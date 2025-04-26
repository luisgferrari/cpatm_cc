package application.model;

public enum TipoArquivo {
    
    FLIGHTS("flighs"),
    CONFIG("config"),
    SECT_CONFIG("sect_config"),
    DESCONHECIDO("desconhecido");

    private final String label;

    private TipoArquivo(String label){
        this.label = label;
    }

    @Override
    public String toString(){
        return label;
    }
}
