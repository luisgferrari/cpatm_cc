package application.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.File;

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

public class MainView extends JFrame {

    private static final int PADDING = 5;
    private JTable tabela;
    private DefaultTableModel tabelaModel; 
    private JButton btnSelecionar, btnValidar, btnSair;
    private JRadioButton rbDetalhar;
    private JProgressBar progressBar;
    private File[] arquivosSelecionados = new File[0];
    private final MainController controller = new MainController();    

    //listeners
    private final ActionListener selecionarArquivosAction = e -> {
        arquivosSelecionados = controller.selecionarArquivos(this);
        adicionarArquivosNaTabela();
        atualizarEstadoBotaoValidar();
    };
    private final ActionListener sairAction = e ->{
        System.exit(0);
    };
    private final ActionListener removerArquivosAction = e -> {
        int[] linhasSelecionadas = tabela.getSelectedRows();
        if(linhasSelecionadas.length > 0){
            int confirmacao = JOptionPane.showConfirmDialog(this, "Remover arquivos selecionados?", "Remover arquivos", JOptionPane.YES_NO_OPTION);
            if (confirmacao == JOptionPane.YES_OPTION) {
                for (int i = linhasSelecionadas.length -1; i >= 0; i--){
                    tabelaModel.removeRow(linhasSelecionadas[i]);
                }

                atualizarEstadoBotaoValidar();
            }
        }
    };
    private final ActionListener validarAction = e -> {
        btnValidar.setEnabled(false);
        boolean detalhar = rbDetalhar.isSelected();
        progressBar.setVisible(true);

        new javax.swing.SwingWorker<Void,Integer>() {
            @Override
            protected Void doInBackground() {
                int qtdArquivos = tabelaModel.getRowCount();
                progressBar.setMaximum(qtdArquivos);
                progressBar.setValue(0);

                for (int i = 0; i < qtdArquivos; i++){
                    String nomeArquivo = tabelaModel.getValueAt(i, 0).toString();
                    String tipo = tabelaModel.getValueAt(i, 1).toString();
                    
                    try {
                        Thread.sleep(100);
                        boolean sucesso = controller.validarArquivo(nomeArquivo, tipo, detalhar);

                        if (sucesso) {
                            tabelaModel.setValueAt("OK", i, 2);
                        } else {
                            tabelaModel.setValueAt("Tipo Inválido", i, 2);
                        }
                    } catch (Exception e) {
                        tabelaModel.setValueAt("Erro", i, 2);
                    }

                    //atualiza a progressBar
                    final int progresso = i + 1;
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(progresso);
                    }
                        
                    );

                }
                return null;
            }
            @Override
            protected void done() {
                progressBar.setVisible(false);
            }
        }.execute();

    };

    public MainView() {
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
        tabelaModel = new DefaultTableModel(colunas, 0);
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
        for (File f : arquivosSelecionados) {
            String nome = f.getAbsolutePath();
            String tipo = controller.identificarTipoDePlanilha(nome);
            String status = "";

            tabelaModel.addRow(new Object[]{nome, tipo, status});
        }
    }

    private void atualizarEstadoBotaoValidar() {
        Boolean tabelaTemArquivos = tabela.getRowCount() > 0;

        if (tabelaTemArquivos) {
            btnValidar.setEnabled(true);
        } else {
            btnValidar.setEnabled(false);
        }
    }
}