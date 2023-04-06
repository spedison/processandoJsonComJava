package br.com.spedison;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public class Main {

    static record DadosArquivo(String nomeArquivo, String estado, Double lat, Double lng) {
        public String toCsv() {
            return "%s\t%s\t%f\t%f".formatted(nomeArquivo.replace(".json", "").trim(), estado,
                    Objects.requireNonNullElse(lat, -99999999),
                    Objects.requireNonNullElse(lng, -99999999)
            );
        }
    }

    public static DadosArquivo processaArquivo(File nomeArquivo) {
        File f = nomeArquivo;
        try {
            //read json file data to String
            byte[] jsonData = Files.readAllBytes(f.toPath());
            JsonNode parent = new ObjectMapper().readTree(jsonData);
            String ok = parent.get("status").asText();
            Double lat = parent.get("results").get(0).get("geometry").get("location").get("lat").asDouble();
            Double lng = parent.get("results").get(0).get("geometry").get("location").get("lng").asDouble();
            DadosArquivo da = new DadosArquivo(f.getName(), ok, lat, lng);
            return da;
        } catch (IOException eio) {
            return new DadosArquivo(nomeArquivo.getName(), "NOK", null, null);
        }
    }

    public static void main(String[] args) throws IOException {

        File listaArquivos = new File("/home/spedison/processaJson/entrada");
        File[] arquivosParaProcessar =
                listaArquivos
                        .listFiles
                                (
                                        (dir, name) ->
                                                name.split("[.]")[1].trim().equalsIgnoreCase("json")
                                );

        File out = new File("/home/spedison/processaJson/saidaProcessamento.csv");
        FileUtils.write(out, "COD_LOCAL\tOK?\tlat\tlng\n", StandardCharsets.UTF_8, false);
        Arrays
                .stream(arquivosParaProcessar)
                .map(Main::processaArquivo)
                .map(DadosArquivo::toCsv)
                .map(s -> "%s\n".formatted(s))
                .forEach(s ->
                        {
                            try {
                                FileUtils.write(out, s, StandardCharsets.UTF_8, true);
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                );
    }
}