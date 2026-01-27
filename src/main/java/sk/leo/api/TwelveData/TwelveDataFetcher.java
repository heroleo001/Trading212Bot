package sk.leo.api.TwelveData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.leo.api.records.RelevantStockData;
import sk.leo.logic.TradeHelper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TwelveDataFetcher {
    private final String apiKey;

    private static final AtomicInteger REQUEST_THIS_DAY = new AtomicInteger(0);
    private static final int DAILY_LIMIT = 800;

    public TwelveDataFetcher(String apiKey) {

        this.apiKey = apiKey;
    }

    public Optional<RelevantStockData> fetchRelevantStockData(String symbol) throws IOException, InterruptedException {

        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClient.newHttpClient();

        if (REQUEST_THIS_DAY.incrementAndGet() > DAILY_LIMIT) {
            throw new IllegalStateException("Daily API limit exceeded");
        }

        String url =
                "https://api.twelvedata.com/time_series" +
                        "?symbol=" + symbol +
                        "&interval=1day" +
                        "&outputsize=2" +
                        "&apikey=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null; // skip failed symbol
        }

        JsonNode root = mapper.readTree(response.body());

        // Market closed, invalid symbol, etc.
        if (!root.has("values")) {
            return null;
        }

        JsonNode values = root.get("values");
        if (values.size() < 2) {
            return null;
        }

        double todayClose = values.get(0).get("close").asDouble();
        double yesterdayClose = values.get(1).get("close").asDouble();
        double percentualChange = (todayClose - yesterdayClose) / yesterdayClose;
        // fraction, NOT percent


        return Optional.of(new RelevantStockData(percentualChange, TradeHelper.round2(todayClose)));
    }


    public Optional<String> resolveSymbolByIsin(String isin) throws Exception {
        if (REQUEST_THIS_DAY.get() >= DAILY_LIMIT) return Optional.empty();

        String url =
                "https://api.twelvedata.com/symbol_search" +
                        "?isin=" + isin +
                        "&apikey=" + apiKey;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> res =
                client.send(req, HttpResponse.BodyHandlers.ofString());
        REQUEST_THIS_DAY.incrementAndGet();

        System.out.println("Response: " + res.body());

        JsonNode root = new ObjectMapper().readTree(res.body());
        JsonNode data = root.get("data");

        if (data != null && !data.isEmpty()) {
            return Optional.of(data.get(0).get("symbol").asText());
        }
        return Optional.empty();
    }
}
