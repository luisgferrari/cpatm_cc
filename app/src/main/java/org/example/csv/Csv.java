package org.example.csv;

import org.example.model.Linha;
import org.example.util.LoggerUtil;

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

/**
 * Classe genérica para entrada e saída de dados através de arquivos CSV
 *
 * @author codrilo
 */
public class Csv {

    private static final Logger log = LoggerUtil.getLogger();
    public Csv(){
        log.info("Csv inicializada");
    }

    /**
     * Reads from a CSV file
     * @param path path to CSV file.
     * @return List containing read lines and the addresses of said lines in the file.
     */
    public static List<Linha> getLines(Path path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            List<Linha> lines = new ArrayList<>();
            String csvLine;
            int address = 0;

            csvLine = br.readLine();
            while (csvLine != null) {
                Linha line = new Linha(++address, csvLine);
                lines.add(line);
                csvLine = br.readLine();
            }

            return lines;
        } catch (IOException e) {
            log.warning("I/O error while reading :" + path + ". Details: " + e.getMessage());
            throw new IOException("Falha ao processar o arquivo CSV: " + path.getFileName(), e);
        }
    }

    /**
     * Writes a CSV file
     *
     * @param content Content to be written
     * @param path destination path
     */
    public static void writeCSVFile(List<String> content, Path path) throws IOException {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to create parent directories for path: " + path.getParent(), e);
            throw new IOException("Falha ao escrever arquivo CSV: " + path.getFileName(), e);
        }

        try {
            Files.write(path, content);
        } catch (IOException e) {
            log.log(Level.WARNING, "I/O error while writing CSV file: " + path, e);
            throw new IOException("Falha ao escrever o arquivo CSV: " + path.getFileName(), e);
        }
    }
}
