package sk.leo.logic.local;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LocalStorer {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final Path BASE_DIR;

    private static final Map<String, String> TICKER_SYMBOL_MAP = new HashMap<>();

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

    public static void storeTickerSymbolMapping(String ticker, String symbol) {
        TICKER_SYMBOL_MAP.put(ticker, symbol);
        storeMappingToFile();
    }

    public static void storeMappingToFile(){
        Path path = BASE_DIR
                .resolve("mapping")
                .resolve("t212_to_symbol.json");

        try {
            Files.createDirectories(path.getParent());
            MAPPER.writeValue(path.toFile(), TICKER_SYMBOL_MAP);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
