package cc;

import cc.view.SelecaoDeArquivos;

/**
 *
 * @author luisg
 */
public class CC {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        Path path_Config = Path.of("C:\\Users\\luisg\\Desktop\\05. Mai_24\\20240501_config.csv");
//        PlanilhaConfig.verificarIntegridade(path_Config);

//        Path path_Sect_Config = Path.of("C:\\Users\\luisg\\Desktop\\05. Mai_24\\20240501_sect_config.csv");
//        PlanilhaSectConfig.verificarIntegridade(path_Sect_Config);
        
        SelecaoDeArquivos janela = new SelecaoDeArquivos();
        janela.setVisible(true);
    }

}
