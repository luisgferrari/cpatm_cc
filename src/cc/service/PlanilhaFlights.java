package cc.service;

import cc.csv.Csv;
import cc.entity.Linha;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Classe para processamento de dados oriundos de planilhas _flights.csv
 *
 * @author luisg
 */
public class PlanilhaFlights extends Planilha {

    public static final String CABECALHO = "timestamp;config_id;sect_config;CTR;ASS;sector;#sectors;#ASS;CALLSIGN;ADEP;ADES;DOF;EOBT;SSR;flrul;";
    //Devido ao campo timestamp utilizar ';' para separar a DATE do TIME foi necessário adicionar '+1' à QTD_CAMPOS
    public static final int QTD_CAMPOS = CABECALHO.split(";").length + 1;
    public static final String SUFIXO_ARQUIVO = "_flights.csv";

    public static List<String> verificarIntegridade(Path inputFile) {
        List<String> relatorioIntegridade = new ArrayList<>();
        relatorioIntegridade.add("RELATÓRIO DE INTEGRIDADE - " + inputFile.getFileName().toString());

        try {
            List<Linha> linhasDoArquivo = Csv.lerLinhas(inputFile);

            localizarCabecalho(linhasDoArquivo, relatorioIntegridade, CABECALHO);
            verificarQuantidadeDeCampos(linhasDoArquivo, relatorioIntegridade, QTD_CAMPOS);
            verificarCamposVazios(linhasDoArquivo, relatorioIntegridade);

            validarLinhas(linhasDoArquivo, relatorioIntegridade);

//            contarQtdLihas(linhasDoArquivo, relatorioIntegridade);
//            verificarHorarios(linhasDoArquivo, relatorioIntegridade);
            Path outputFile = Paths.get(inputFile.getParent().toString(), inputFile.getFileName().toString().replace(".csv", ".txt"));
            Csv.escrever(relatorioIntegridade, outputFile);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            Path outputFile = Paths.get(inputFile.getParent().toString(), inputFile.getFileName().toString().replace(".csv", "-ERRO.txt"));
            relatorioIntegridade.add(e.getLocalizedMessage());
            Csv.escrever(relatorioIntegridade, outputFile);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            Path outputFile = Paths.get(inputFile.getParent().toString(), inputFile.getFileName().toString().replace(".csv", "-ERRO.txt"));
            relatorioIntegridade.add(e.getLocalizedMessage());
            Csv.escrever(relatorioIntegridade, outputFile);
        }

        return relatorioIntegridade;
    }

    private static void validarLinhas(List<Linha> linhasDoArquivo, List<String> relatorioIntegridade) {
        //Remover linha abaixo
//        relatorioIntegridade.clear();
        relatorioIntegridade.add("\nCONDIÇÃO: CAMPO INVÁLIDO");

        Iterator<Linha> iterador = linhasDoArquivo.iterator();
        List<Linha> linhasInvalidas = new ArrayList<>();
        List<String> listaErros = new ArrayList<>();

        while (iterador.hasNext()) {
            Linha linha = iterador.next();
            String erro = validarLinha(linha);
            if (!erro.isBlank()) {
                listaErros.add(erro);
                linhasInvalidas.add(linha);
            }
        }

        if (listaErros.isEmpty()) {
            relatorioIntegridade.add("\tNenhuma linha com erro");
        } else {
            int indice = 0;
            for (String erro : listaErros) {
                relatorioIntegridade.add(String.format("\tLinha %4d - %s - %s", linhasInvalidas.get(indice).getEndereco(), erro, linhasInvalidas.get(indice++).getConteudo()));
            }
            relatorioIntegridade.add("\tInválidas: " + indice);
        }
    }

    private static String validarLinha(Linha linha) {
        String[] campos = linha.getConteudo().split(";");
        if (campos.length != QTD_CAMPOS) {
            return String.format("Qtd campos incorreta. Esperados %d, encontrados %d", QTD_CAMPOS, campos.length);
        } else {
            List<String> errosNaLinha = new ArrayList<>();
            for (int i = 0; i < campos.length; i++) {
                errosNaLinha.add(validarCampo(campos[i], i));
            }

            if (errosNaLinha.isEmpty()) {
                return null;
            } else {
                String msgDeErro = "";
                for (String erro : errosNaLinha) {
                    msgDeErro = msgDeErro.concat(erro);
                }
                return String.format("%s", msgDeErro);
            }
        }
    }

    private static String validarCampo(String campo, int indice) {
        Pattern pattern;
        String regex;
        String msgErro = "";

        switch (indice) {
            case 0: //valida data_timestamp
                regex = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$";
                msgErro = "Data: ";
                break;
            case 1: //valida hora_timestamp
                regex = "^(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
                msgErro = "Hora: ";
                break;
            case 2: //valida config_id
                regex = "^\"([2-9]|1[0-8]).[0-9]{1,5}.([0-9]|1[0-8])\"$";
                msgErro = "config_id: ";
                break;
            //NÃO IMPLEMENTADA
//            case 3: //valida sect_config
//                msgErro = "sect_config: ";
//                break;
            case 4: //valida ctr
                regex = "^\"CTR([0-1][0-9]|2[0-1])\"$";
                msgErro = "CTR: ";
                break;
            case 5: //valida ass
                regex = "^\"(ASS([0-1][0-9]|2[0-1])|)\"$";
                msgErro = "ASS: ";
                break;
            case 6: //valida sector
                regex = "^\"(S0[1-9]{1}|S1[0-8]{1}|S6F|18F)\"$";
                msgErro = "sector: ";
                break;
            case 7: //valida qtd_sectors
                regex = "^([1-9]{1}|1[0-8])$";
                msgErro = "qtd_sector: ";
                break;
            case 8: //valida qtd_ass
                regex = "^([0-9]{1}|1[0-8]{1})$";
                msgErro = "qtd_ass: ";
                break;
            case 9: //valida callsign
                regex = "^([a-zA-Z0-9]{5,7})$";
                msgErro = "callsign: ";
                break;
            case 10: //valida ADEP
                regex = "^([A-Z]{2}[A-Z0-9]{2})$";
                msgErro = "ADEP: ";
                break;
            case 11: //valida ADES
                regex = "^([A-Z]{2}[A-Z0-9]{2})$";
                msgErro = "ADES: ";
                break;
            case 12: //valida DOF
                regex = "^([0-9]{6})$";
                msgErro = "DOF: ";
                break;
            case 13: //valida EOBT
                regex = "^([0-1][0-9]|2[0-3])[0-5][0-9]$";
                msgErro = "EOBT: ";
                break;
            case 14: //valida SSR
                regex = "^A[0-7]{4}$";
                msgErro = "SSR: ";
                break;
            case 15: //valida flrul
                regex = "^(I|V|Y|Z)$";
                msgErro = "flrul: ";
                break;
            default:
                return "";
        }
        pattern = Pattern.compile(regex);
        if (!pattern.matcher(campo).matches()) {
            msgErro = msgErro.concat(campo);
            return "|" + msgErro + "|";
        }
        return "";
    }
}
