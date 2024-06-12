package cc.view;

import cc.service.PlanilhaConfig;
import cc.service.PlanilhaFlights;
import cc.service.PlanilhaSectConfig;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A classe implementa uma janela para selecionar arquivos CSV com dados
 * oriundos do SAGITARIO para que sejam processados.
 *
 * @author luisg
 */
public class SelecaoDeArquivos extends JFrame {

    private final JTextArea textArea;
    private final JProgressBar progressBar;

    /**
     * Construtor da classe. Cria uma janela que permite ao usuário selecionar
     * um ou mais arquivos CSV. Para cada arquivo é chamado um método de
     * validação correspondente com base no nome do arquivo.
     */
    public SelecaoDeArquivos() {
        setTitle("CC - Selecionar Arquivos");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        progressBar = new JProgressBar();

        add(scrollPane, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

        // Criando o FileDialog para abrir arquivos
        FileDialog fileDialog = new FileDialog(this, "CC - Selecionar Arquivos", FileDialog.LOAD);
        fileDialog.setMultipleMode(true);
        fileDialog.setVisible(true);
        File[] arquivos = fileDialog.getFiles();

        if (arquivos.length != 0) {
            new FileProcessorWorker(arquivos).execute();
        } else {
            JOptionPane.showMessageDialog(null, "Nenhum arquivo selecionado", "Aviso", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        setVisible(true);
    }

    private class FileProcessorWorker extends SwingWorker<Void, String> {

        private final File[] arquivos;
        private final List<String> arquivosNaoValidados = new ArrayList<>();

        public FileProcessorWorker(File[] arquivos) {
            this.arquivos = arquivos;
        }

        @Override
        protected Void doInBackground() {
            int total = arquivos.length;
            int count = 0;

            for (File arquivo : arquivos) {
                Path path = Path.of(arquivo.getPath());
                String sufix = path.getFileName().toString().substring(8);

                if (sufix.endsWith(".csv")) {
                    try {
                        if (sufix.equalsIgnoreCase(PlanilhaConfig.SUFIXO_ARQUIVO)) {
                            PlanilhaConfig.verificarIntegridade(path);
                        } else if (sufix.equalsIgnoreCase(PlanilhaSectConfig.SUFIXO_ARQUIVO)) {
                            PlanilhaSectConfig.verificarIntegridade(path);
                        } else if (sufix.equalsIgnoreCase(PlanilhaFlights.SUFIXO_ARQUIVO)) {
                            PlanilhaFlights.verificarIntegridade(path);
                        } else {
                            arquivosNaoValidados.add(arquivo.getName());
                        }
                        publish("Processado: " + arquivo.getName());
                    } catch (Exception e) {
                        arquivosNaoValidados.add(arquivo.getName());
                        publish("Erro ao processar: " + arquivo.getName());
                    }
                } else {
                    arquivosNaoValidados.add(arquivo.getName());
                    publish("Formato inválido: " + arquivo.getName());
                }

                count++;
                setProgress((int) ((count / (float) total) * 100));
            }
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String message : chunks) {
                textArea.append(message + "\n");
            }
        }

        @Override
        protected void done() {
            if (!arquivosNaoValidados.isEmpty()) {
                JTextArea textArea = new JTextArea(10, 30);
                textArea.setText(String.join("\n", arquivosNaoValidados));
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 200));

                JOptionPane.showMessageDialog(SelecaoDeArquivos.this, scrollPane, "Arquivos não validados", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(SelecaoDeArquivos.this, "Todos os arquivos foram processados com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            System.exit(0);
        }
    }

}
