package application.model;

import java.nio.file.Path;
import application.service.PlanilhaConfig;
import application.service.PlanilhaFlights;
import application.service.PlanilhaSectConfig;

public class ArquivoCSV {
    
    private final Path path;
    private final TipoArquivo tipo;
    private StatusArquivo status;
    
    
    public ArquivoCSV(Path path){
        this.path = path;
        this.tipo = identificarTipo(path);
        this.status = identificarStatus();
    };
    
    // getters
    public Path getPath() {
        return path;
    }
    
    public TipoArquivo getTipo() {
        return tipo;
    }
    
    public StatusArquivo getStatus() {
        return status;
    }
    
    // setter
    public void setStatus(StatusArquivo status) {
        this.status = status;
    }
    
    // Métodos privados auxiliares
    private TipoArquivo identificarTipo(Path path){
        String nome = path.getFileName().toString();
        if (nome.endsWith(PlanilhaFlights.SUFIXO)) return TipoArquivo.FLIGHTS;
        if (nome.endsWith(PlanilhaSectConfig.SUFIXO)) return TipoArquivo.SECT_CONFIG;
        if (nome.endsWith(PlanilhaConfig.SUFIXO))return TipoArquivo.CONFIG;
        return TipoArquivo.DESCONHECIDO;
    }

    private StatusArquivo identificarStatus(){
        if (this.tipo == TipoArquivo.DESCONHECIDO) return StatusArquivo.TIPO_DESCONHECIDO;
        return StatusArquivo.PRONTO;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArquivoCSV that = (ArquivoCSV) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
