package application.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import application.controller.MainController;
import application.model.ArquivoCSV;
import application.model.StatusArquivo;
import application.util.LoggerUtil;

public class MainView extends JFrame {

    private static final Logger log = LoggerUtil.getLogger();
    private static final int PADDING = 5;
    private JTable tabela;
    private DefaultTableModel tabelaModel; 
    private JButton btnSelecionar, btnValidar, btnSair;
    private JRadioButton rbDetalhar;
    private JProgressBar progressBar;
    private List<ArquivoCSV> arquivosSelecionados = new ArrayList<>();
    private List<ArquivoCSV> arquivosTabela = new ArrayList<>();
    private final MainController controller = new MainController();    

    //listeners
    private final ActionListener selecionarArquivosAction = e -> {
        arquivosSelecionados = controller.selecionarArquivos(this);
        adicionarArquivosNaTabela();
        atualizarTabela();
        log.info("Arquivos selecionados: " + arquivosSelecionados.size());
    };

    private final ActionListener sairAction = e ->{
        log.info("Botão sair acionado.");
        System.exit(0);
    };

    private final ActionListener removerArquivosAction = e -> {
        int[] linhasSelecionadas = tabela.getSelectedRows();
        if(linhasSelecionadas.length > 0){
            int confirmacao = JOptionPane.showConfirmDialog(this, "Remover arquivos selecionados?", "Remover arquivos", JOptionPane.YES_NO_OPTION);
            if (confirmacao == JOptionPane.YES_OPTION) {
                log.info("Removendo " + linhasSelecionadas.length + " arquivos da tabela.");
                removerArquivosDaTabela();
            }
        }
    };

    private final ActionListener validarAction = e -> {
        btnValidar.setEnabled(false);
        boolean detalhar = rbDetalhar.isSelected();
        log.info("Iniciando validação com " + tabela.getRowCount() + " arquivos. detalhar=" + detalhar);
        progressBar.setVisible(true);

        new javax.swing.SwingWorker<Void,Integer>() {
            @Override
            protected Void doInBackground() {
                int qtdArquivos = arquivosTabela.size();
                progressBar.setMaximum(qtdArquivos);
                progressBar.setValue(0);

                for (int i = 0; i < qtdArquivos; i++){
                    ArquivoCSV arquivoCSV = arquivosTabela.get(i);
                    
                    try {
                        controller.validarArquivo(arquivoCSV, detalhar);
                    } catch (Exception e) {
                        arquivoCSV.setStatus(StatusArquivo.ERRO);
                    }

                    //atualiza a progressBar
                    final int progresso = i + 1;
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(progresso);
                    });
                }
                return null;
            }
            @Override
            protected void done() {
                atualizarTabela();
                progressBar.setVisible(false);
                log.info("Validação encerrada");
            }
        }.execute();
    };

    public MainView() {
        log.info("MainView inicializada");
        setTitle("CC - Validação de .csv");
        setSize(600,400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComponents();
        setVisible(true);
    }
    
    private void initComponents() {
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setLayout(new BorderLayout());

        //tabela
        String[] colunas = {"Arquivo", "Tipo", "Status"};
        tabelaModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };
        tabela = new JTable(tabelaModel);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(380);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabela.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane jScrollPane = new JScrollPane(tabela);
        
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        tabela.getColumnModel().getColumn(1).setCellRenderer(centralizado);
        tabela.getColumnModel().getColumn(2).setCellRenderer(centralizado);

        //progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        JPanel painelCentro = new JPanel(new BorderLayout());
        painelCentro.add(jScrollPane, BorderLayout.CENTER);
        painelCentro.add(progressBar, BorderLayout.SOUTH);
        add(painelCentro, BorderLayout.CENTER);

        //painel direita
        btnSair = new JButton("Sair");
        btnSair.addActionListener(sairAction);
        btnValidar = new JButton("Validar");
        btnValidar.setEnabled(false);
        btnValidar.addActionListener(validarAction);
        rbDetalhar = new JRadioButton("Detalhar");
        
        JPanel painelDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelDireita.add(rbDetalhar);
        painelDireita.add(btnValidar);
        painelDireita.add(btnSair);
        
        //painel esquerda
        btnSelecionar = new JButton("Selecionar Arquivos");
        btnSelecionar.addActionListener(selecionarArquivosAction);

        JPanel painelEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelEsquerda.add(btnSelecionar);

        //painel botões
        JPanel painelBotoes = new JPanel(new BorderLayout());
        painelBotoes.add(painelEsquerda, BorderLayout.WEST);
        painelBotoes.add(painelDireita, BorderLayout.EAST);
        add(painelBotoes, BorderLayout.SOUTH);

        //popup remover itens
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem itemRemover = new JMenuItem("Remover arquivos");
        itemRemover.addActionListener(removerArquivosAction);
        popupMenu.add(itemRemover);
        tabela.setComponentPopupMenu(popupMenu);
    }

    private void adicionarArquivosNaTabela() {
        List<ArquivoCSV> arquivosCSVDuplicados = new ArrayList<>();

        for (ArquivoCSV arquivoCSV : arquivosSelecionados) {
            if (arquivosTabela.contains(arquivoCSV)) {
                arquivosCSVDuplicados.add(arquivoCSV);
            } else {
                arquivosTabela.add(arquivoCSV);
            }
        }

        if (!arquivosCSVDuplicados.isEmpty()){
            StringBuilder mensagem = new StringBuilder("Os arquivos abaixo já estavam selecionados:\n\n");
            for (ArquivoCSV arquivoCSV : arquivosCSVDuplicados) {
                mensagem.append(arquivoCSV.getPath().toString()).append("\n");
            }
            JOptionPane.showMessageDialog(this, mensagem.toString(), "Arquivos Auplicados", JOptionPane.INFORMATION_MESSAGE);
        }

        ordenarArquivosTabela();
    }

    private void removerArquivosDaTabela() {
        int[] linhasSelecionadas = tabela.getSelectedRows();

        if(linhasSelecionadas.length > 0){
            for (int i = linhasSelecionadas.length - 1; i >= 0; i--) {
                arquivosTabela.remove(linhasSelecionadas[i]);
            }
        }
        ordenarArquivosTabela();
        atualizarTabela();
    }

    private void ordenarArquivosTabela() {
        arquivosTabela.sort((a, b) -> a.getPath().getFileName().toString().compareToIgnoreCase(b.getPath().getFileName().toString()));
        atualizarTabela();
    }

    private void atualizarTabela() {
        tabelaModel.setRowCount(0);
        
        for (ArquivoCSV arquivo : arquivosTabela){
            String nome = arquivo.getPath().getFileName().toString();
            String tipo = arquivo.getTipo().toString();
            String status = arquivo.getStatus().toString();

            tabelaModel.addRow(new Object[]{nome, tipo, status});
        }

        atualizarEstadoBotaoValidar();
    }

    private void atualizarEstadoBotaoValidar() {
        Boolean possuiArquivosNaTabela = !arquivosTabela.isEmpty();

        if (possuiArquivosNaTabela) {
            btnValidar.setEnabled(true);
        } else {
            btnValidar.setEnabled(false);
        }
    }
}