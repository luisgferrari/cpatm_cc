package cc.csv;

import cc.entity.Linha;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe genérica para entrada e saída de dados através de arquivos CSV
 *
 * @author luisg
 */
public class Csv {

    /**
     * Realiza a leitura de um arquivo CSV e retorna seu conteúdo e o número da
     * linha em que foi encontrado através de uma lista de Linhas
     *
     * @param caminho o endereço para o arquivo a ser lido
     * @return Lista de Linha com a localização e o conteúdo de cada linha
     * @throws java.io.IOException se ocorrer um erro de leitura do arquivo
     */
    public static List<Linha> lerLinhas(Path caminho) throws IOException {
        List<Linha> conteudo = new ArrayList<>();
        String linhaCSV;
        try (BufferedReader br = new BufferedReader(new FileReader(caminho.toFile()))) {
            linhaCSV = br.readLine();
            int numLinha = 0;
            while (linhaCSV != null) {
                Linha linha = new Linha(++numLinha, linhaCSV);
                conteudo.add(linha);
                linhaCSV = br.readLine();
            }
            
        } catch (IOException e) {
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
        try{
            Files.createDirectories(caminho.getParent());
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho.toFile()))) {
                for (String linha : conteudo) {
                    bw.write(linha);
                    bw.newLine();
                }
        }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}
