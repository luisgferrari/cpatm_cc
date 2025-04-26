package application.controller;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import application.model.ArquivoCSV;
import application.model.TipoArquivo;
import application.service.PlanilhaConfig;
import application.service.PlanilhaFlights;
import application.service.PlanilhaSectConfig;
import application.util.LoggerUtil;


public class MainController {

    private static final Logger log = LoggerUtil.getLogger();

    public MainController(){
        log.info("MainController inicializado");
    }

    public List<ArquivoCSV> selecionarArquivos(JFrame parent) {
        log.info("Selecinando arquivos");
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setMultiSelectionEnabled(true);
        jFileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos CSV", "csv")); 
        List<ArquivoCSV> listaArquivosCSV = new ArrayList<>();

        int resultado = jFileChooser.showOpenDialog(parent);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File[] arquivosSelecionados = jFileChooser.getSelectedFiles();
            for (File f : arquivosSelecionados) {
                ArquivoCSV arquivoCSV = new ArquivoCSV(f.toPath());      
                listaArquivosCSV.add(arquivoCSV);         
            }
        }

        return listaArquivosCSV;
    }
    
    public boolean validarArquivo(ArquivoCSV arquivoCSV, boolean detalhar) {
        TipoArquivo tipoArquivo = arquivoCSV.getTipo();
        Path path = arquivoCSV.getPath();
        log.info("Iniciando validação de: " + path);

        switch (tipoArquivo) {
            case CONFIG:
                return PlanilhaConfig.verificarIntegridade(path, detalhar);
            case SECT_CONFIG:
                return PlanilhaSectConfig.verificarIntegridade(path, detalhar);
            case FLIGHTS:
                return PlanilhaFlights.verificarIntegridade(path, detalhar);
            default:
                log.warning("Tipo de arquivo inválido para validação: " + tipoArquivo.toString());
                return false;
        }
    }
}
