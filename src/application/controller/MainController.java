package application.controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import application.service.PlanilhaConfig;
import application.service.PlanilhaFlights;
import application.service.PlanilhaSectConfig;
import application.util.LoggerUtil;


public class MainController {

    private static final Logger log = LoggerUtil.getLogger();

    public MainController(){
        log.info("MainController inicializado");
    }

    public File[] selecionarArquivos(JFrame parent) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setMultiSelectionEnabled(true);
        jFileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos CSV", "csv")); 
        
        int resultado = jFileChooser.showOpenDialog(parent);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            return jFileChooser.getSelectedFiles();
        }

        return new File[0];
    }

    public String identificarTipoDePlanilha(String nome) {
        if (nome.endsWith(PlanilhaFlights.SUFIXO_ARQUIVO)) {
            return "FLIGHTS";
        } else if (nome.endsWith(PlanilhaSectConfig.SUFIXO_ARQUIVO)) {
            return "SECT_CONFIG";
        } else if (nome.endsWith(PlanilhaConfig.SUFIXO_ARQUIVO)) {
            return "CONFIG";
        }
        return "DESCONHECIDO";
    }
    
    public boolean validarArquivo(String caminhoArquivo, String tipo, boolean detalhar) {
        Path path = Paths.get(caminhoArquivo);
        log.info("Iniciando validação de: " + path);
        
        switch (tipo) {
            case "CONFIG":
                return PlanilhaConfig.verificarIntegridade(path, detalhar);
            case "SECT_CONFIG":
                return PlanilhaSectConfig.verificarIntegridade(path, detalhar);
            case "FLIGHTS":
                return PlanilhaFlights.verificarIntegridade(path, detalhar);
            default:
                log.warning("Tipo inválido: " + tipo + " para " + path);
                return false;
        }
    }
}
