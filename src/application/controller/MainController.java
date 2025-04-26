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
import application.model.StatusArquivo;
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
    
    public void validarArquivo(ArquivoCSV arquivoCSV, boolean detalhar) {
        Path path = arquivoCSV.getPath();
        TipoArquivo tipoArquivo = arquivoCSV.getTipo();
        boolean resultadoValidacao = false;
        log.info("Iniciando validação de: " + path);

        switch (tipoArquivo) {
            case CONFIG:
                resultadoValidacao = PlanilhaConfig.verificarIntegridade(path, detalhar);
                break;
            case SECT_CONFIG:
                resultadoValidacao = PlanilhaSectConfig.verificarIntegridade(path, detalhar);
                break;
            case FLIGHTS:
                resultadoValidacao = PlanilhaFlights.verificarIntegridade(path, detalhar);
                break;
            case DESCONHECIDO:
                log.warning("Tipo de arquivo inválido para validação: " + tipoArquivo.toString());
                arquivoCSV.setStatus(StatusArquivo.TIPO_DESCONHECIDO);
                return;
            default:
                throw new IllegalArgumentException("Tipo de arquivo não tratado: " + tipoArquivo);
        }

        if (resultadoValidacao) {
            arquivoCSV.setStatus(StatusArquivo.VALIDADO);
        } else {
            arquivoCSV.setStatus(StatusArquivo.ERRO);
        }
    }
}
