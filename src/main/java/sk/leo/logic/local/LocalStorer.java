package sk.leo.logic.local;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import sk.leo.api.Auth;
import sk.leo.api.TwelveData.TwelveDataFetcher;
import sk.leo.api.records.Instrument;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

public class LocalStorer {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final Path BASE_DIR;

    static {
        BASE_DIR = Paths.get(
                System.getProperty("user.home"),
                "trading-bot",
                "data"
        );
        try {
            if (Files.notExists(BASE_DIR)) {
                Files.createDirectories(BASE_DIR);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private LocalStorer() {
        // prevent instantiation
    }

    public static Map<String, String> loadTickerSymbolMapping() {
        Path path = BASE_DIR
                .resolve("mapping")
                .resolve("t212_to_symbol.json");

        if (Files.notExists(path)) {
            return new HashMap<>();
        }

        try {
            return MAPPER.readValue(
                    path.toFile(),
                    new TypeReference<Map<String, String>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ticker-symbol mapping", e);
        }
    }

    public static void storeTickerSymbolMapping(Map<String, String> mapping) {
        Path path = BASE_DIR
                .resolve("mapping")
                .resolve("t212_to_symbol.json");

        try {
            Files.createDirectories(path.getParent());
            MAPPER.writeValue(path.toFile(), mapping);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
