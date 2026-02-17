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
    private static Map<String, String> LOADED_TICKER_SYMBOL_MAP = null;

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

    public static Map<String, String> getTickerSymbolMapping() {
        if (LOADED_TICKER_SYMBOL_MAP != null)
            return LOADED_TICKER_SYMBOL_MAP;

        Path path = BASE_DIR
                .resolve("mapping")
                .resolve("t212_to_symbol.json");

        if (Files.notExists(path)) {
            return new HashMap<>();
        }

        try {
            Map<String, String> result = MAPPER.readValue(
                    path.toFile(),
                    new TypeReference<Map<String, String>>() {}
            );
            LOADED_TICKER_SYMBOL_MAP = result;
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ticker-symbol mapping", e);
        }
    }

    public static String getSymbol(String ticker){
        return getTickerSymbolMapping().get(ticker);
    }

    public static void addToTickerSymbolMapping(String ticker, String symbol) {
        TICKER_SYMBOL_MAP.put(ticker, symbol);
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

    @Deprecated
    public static void storeHistoricData(Map<String, List<Double>> priceLists) {
        Path path = BASE_DIR
                .resolve("historic_data")
                .resolve("hist_stock_data.json");

        try {
            Files.createDirectories(path.getParent());
            MAPPER.writeValue(path.toFile(), priceLists);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
