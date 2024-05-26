package cc.view;

import cc.service.PlanilhaConfig;
import cc.service.PlanilhaSectConfig;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author luisg
 */
public class SelecaoDeArquivos extends Frame {

    public SelecaoDeArquivos() {
        // Criando uma janela principal (Frame) invisível
        Frame frame = new Frame();
        frame.setSize(0, 0);
        frame.setLocationRelativeTo(null);

        // Criando o FileDialog para abrir arquivos
        FileDialog fileDialog = new FileDialog(frame, "CC - Selecionar Arquivos", FileDialog.LOAD);
        fileDialog.setMultipleMode(true);
        // Exibindo o FileDialog na inicialização da aplicação
        fileDialog.setVisible(true);

        // Obtendo o arquivo selecionado
        File[] arquivos = fileDialog.getFiles();
        List<String> arquivosNaoValidados = new ArrayList<>();

        if (arquivos.length != 0) {
            for (File arquivo : arquivos) {
                Path path = Path.of(arquivo.getPath());
                String sufix = path.getFileName().toString().substring(8);

                if (sufix.endsWith(".csv")) {
                    if (sufix.equalsIgnoreCase(PlanilhaConfig.SUFIXO_ARQUIVO)) {
                        PlanilhaConfig.verificarIntegridade(path);
                    } else if (sufix.equalsIgnoreCase(PlanilhaSectConfig.SUFIXO_ARQUIVO)) {
                        PlanilhaSectConfig.verificarIntegridade(path);
                    } else {
                        arquivosNaoValidados.add(arquivo.getName());
                    }
                } else {
                    arquivosNaoValidados.add(arquivo.getName());
                }
            }

            if (!arquivosNaoValidados.isEmpty()) {
                String lista = "";
                for (String arquivoNaoValidado : arquivosNaoValidados) {
                    lista = lista + arquivoNaoValidado + "\n";
                }
                JOptionPane.showMessageDialog(null, lista, "Arquivos não validados", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Nenhum arquivo selecionado", "Aviso", JOptionPane.WARNING_MESSAGE);
        }

        // Fechando a aplicação após a seleção do arquivo
        System.exit(0);
    }

}
