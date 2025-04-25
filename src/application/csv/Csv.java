package application.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.entity.Linha;
import application.util.LoggerUtil;

/**
 * Classe genérica para entrada e saída de dados através de arquivos CSV
 *
 * @author luisg
 */
public class Csv {

    private static final Logger log = LoggerUtil.getLogger();
    public Csv(){
        log.info("Csv inicializada");
    }

    /**
     * Realiza a leitura de um arquivo CSV e retorna seu conteúdo e o número da
     * linha em que foi encontrado através de uma lista de Linhas
     *
     * @param caminho o endereço para o arquivo a ser lido
     * @return Lista de Linha com a localização e o conteúdo de cada linha
     * @throws java.io.IOException se ocorrer um erro de leitura do arquivo
     */
    public static List<Linha> lerLinhas(Path caminho) throws IOException {
        log.info("Lendo arquivo csv " + caminho);
        List<Linha> conteudo = new ArrayList<>();
        String linhaCSV = null;
        int numLinha = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(caminho.toFile()))) {
            linhaCSV = br.readLine();
            while (linhaCSV != null) {
                Linha linha = new Linha(++numLinha, linhaCSV);
                conteudo.add(linha);
                linhaCSV = br.readLine();
            }
            
        } catch (IOException e) {
            log.log(Level.WARNING, "Exceção ao ler arquivo CSV", e);

            if (numLinha > 0){
                log.log(Level.INFO, "Última linha lida " + numLinha);
            }

            if(linhaCSV != null && !linhaCSV.isEmpty())
            log.log(Level.INFO, "Conteúdo da última linha lida: " + linhaCSV);
            throw e;
        }
        
        return conteudo;
    }

    /**
     * Realiza a escrita de um arquivo CSV a partir de uma lista
     *
     * @param conteudo Conteúdo a ser escrito no arquivo CSV
     * @param caminho Caminho onde o arquivo deve ser escrito
     */
    public static void escrever(List<String> conteudo, Path caminho) {
        log.info("Escrevendo arquivo csv " + caminho);
        try{
            Files.createDirectories(caminho.getParent());
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho.toFile()))) {
                for (String linha : conteudo) {
                    bw.write(linha);
                    bw.newLine();
                }
        }
        } catch (IOException e) {
            log.log(Level.WARNING, "Exceção ao escrever arquivo CSV", e);
        }
    }

    public static void escrever(List<String> conteudo, Path caminho, Boolean append) {
        log.info("Escrevendo arquivo csv " + caminho);
        try{
            Files.createDirectories(caminho.getParent());
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho.toFile(), append))) {
                for (String linha : conteudo) {
                    bw.write(linha);
                    bw.newLine();
                }
        }
        } catch (IOException e) {
            log.log(Level.WARNING, "Exceção ao escrever arquivo CSV", e);
        }
    }

}
