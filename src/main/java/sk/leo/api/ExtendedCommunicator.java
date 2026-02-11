package sk.leo.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.leo.api.records.*;
import sk.leo.logic.TradeHelper;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExtendedCommunicator extends RateLimitedCommunicator {
    private final Supplier<Instrument[]> instruments;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ExtendedCommunicator(String header, Supplier<Instrument[]> instruments) {
        super(header);
        this.instruments = instruments;
    }

    public void buyMarket(String ticker,
                          double quantity) {

        System.out.println("buy MARKET: " + ticker + "\t" + quantity);

        if (quantity == 0) {
            System.out.println("Selling zero");
            return;
        }

        ServiceCall<Requests.MarketRequest, EmptyRecord> call = new ServiceCall<>(
                ServiceCallType.PLACE_MARKET_ORDER,
                null,
                new Requests.MarketRequest(ticker, quantity, supportsExtendedHours(ticker)),
                new TypeReference<EmptyRecord>() {},
                (ignore, ignore1) -> {}
        );
        callService(call);
    }


    public void sellMarket(String ticker,
                           double quantity) {
        buyMarket(ticker, -quantity);
    }

    public void sellPosition(Position toSell) {
        System.out.println("Selling " + toSell.quantityAvailableForTrading() + " " + toSell.name() + " stock");

        sellMarket(toSell.ticker(), toSell.quantityAvailableForTrading());
    }

    private Optional<Map<String, Instrument>> getInstrumentsAsMap() {
        if (instruments.get() == null)
            return Optional.empty();
        return Optional.of(Arrays.stream(instruments.get())
                .collect(Collectors.toMap(
                        Instrument::ticker,
                        p -> p
                )));
    }

    private boolean supportsExtendedHours(String ticker) {
        Optional<Map<String, Instrument>> instrumentMap = getInstrumentsAsMap();
        if (instrumentMap.isPresent())
            return instrumentMap.get().get(ticker).extendedHours();
        return false;
    }

    public void resolveSymbolByIsin(String isin, BiConsumer<EmptyRecord, String> onResult) {
        ServiceCall<EmptyRecord, EmptyRecord> serviceCall = new ServiceCall<>(
                ServiceCallType.SYMBOL_SEARCH,
                Map.of(UrlParamType.ISIN, isin, UrlParamType.API_KEY, Auth.getTdApiKey()),
                null,
                new TypeReference<EmptyRecord>() {},
                onResult
        );
        callService(serviceCall);
    }

    public void fetchRelevantStockData(String symbol, BiConsumer<EmptyRecord, String> onResult) {
        ServiceCall<EmptyRecord, EmptyRecord> serviceCall = new ServiceCall<>(
                ServiceCallType.TIME_SERIES,
                Map.of(
                        UrlParamType.SYMBOL, symbol,
                        UrlParamType.API_KEY, Auth.getTdApiKey(),
                        UrlParamType.OUTPUT_SIZE, "2",
                        UrlParamType.TIME_INTERVAL, "1day"),
                null,
                new TypeReference<EmptyRecord>() {},
                onResult
        );
        callService(serviceCall);
    }

    public void getExchangeRateToEur(String currency, BiConsumer<CurrencyExchangeRate, String> onResult){
        ServiceCall<EmptyRecord, CurrencyExchangeRate> serviceCall = new ServiceCall<>(
                ServiceCallType.EXCHANGE_RATE,
                Map.of(UrlParamType.SYMBOL, currency + "/EUR", UrlParamType.API_KEY, Auth.getTdApiKey()),
                null,
                new TypeReference<CurrencyExchangeRate>() {},
                onResult
        );
        callService(serviceCall);
    }

    public static Optional<String> parseSymbolFromResponseBody(String body) {
        try {
            JsonNode root = MAPPER.readTree(body);
            JsonNode data = root.get("data");
            if (data.isEmpty()) return Optional.empty();
            return Optional.of(data.get(0).get("symbol").asText());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<RelevantStockData> parseStockDataFromResponseBody(String body) {
        try {
            JsonNode root = MAPPER.readTree(body);

            if (isErrorBodyNode(root)) return Optional.empty();

            // Market closed, invalid symbol, etc.
            if (!root.has("values")) return Optional.empty();

            JsonNode values = root.get("values");
            if (values.size() < 2) return Optional.empty();

            double todayClose = values.get(0).get("close").asDouble();
            double yesterdayClose = values.get(1).get("close").asDouble();
            double percentualChange = (todayClose - yesterdayClose) / yesterdayClose;
            // fraction, NOT percent

            String currency = root.get("meta").get("currency").asText();

            double roundedPrice = TradeHelper.round2(todayClose);
            if (roundedPrice == 0.0) return Optional.empty();

            return Optional.of(new RelevantStockData(percentualChange, roundedPrice, currency));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isErrorBodyNode(JsonNode root){
        if (root.has("code")){
            System.out.println("Error code: " + root.get("code").asText());
            return true;
        } else {
            return false;
        }
    }
}
