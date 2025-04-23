package application.controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import application.service.PlanilhaConfig;
import application.service.PlanilhaFlights;
import application.service.PlanilhaSectConfig;

public class MainController {

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
        
        switch (tipo) {
            case "CONFIG":
                PlanilhaConfig.verificarIntegridade(path, detalhar);
                break;
            case "SECT_CONFIG":
                PlanilhaSectConfig.verificarIntegridade(path, detalhar);
                break;
            case "FLIGHTS":
                PlanilhaFlights.verificarIntegridade(path, detalhar);
                break;
            default:
                return false;
        }
        return true;
    }
}
