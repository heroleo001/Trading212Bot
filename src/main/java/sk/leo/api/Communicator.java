package sk.leo.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.leo.api.records.AccountSummary;
import sk.leo.api.records.Position;
import sk.leo.api.records.Requests.*;
import sk.leo.api.records.ResolvedEndpoint;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * This class can be used for testing, but permanent usage is unrecommended as it doesn't take RATE-LIMITS into account
 */
@Deprecated
public class Communicator {

    private static final String BASE_URL =
            "https://demo.trading212.com";
    private final String header;

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
    );
    private final HttpClient CLIENT = HttpClient.newHttpClient();

    public Communicator(String header){
        this.header = header;
    }

    public HttpResponse<String> call(
            ServiceCallType endpoint,
            Map<String, String> pathParams,
            Object body
    ) throws Exception {

        ResolvedEndpoint ep = EndpointResolver.resolve(endpoint);

        String path = ep.path();

        if (pathParams != null) {
            for (var e : pathParams.entrySet()) {
                path = path.replace("{" + e.getKey() + "}", e.getValue());
            }
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", header)
                .header("Content-Type", "application/json");

        if (body != null) {
            builder.method(
                    ep.method(),
                    HttpRequest.BodyPublishers.ofString(
                            MAPPER.writeValueAsString(body)
                    )
            );
        } else {
            builder.method(
                    ep.method(),
                    HttpRequest.BodyPublishers.noBody()
            );
        }

        return CLIENT.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    public Map<String, Position> getPositions() throws Exception {

        HttpResponse<String> response = call(
                ServiceCallType.GET_OPEN_POSITIONS,
                null,
                null
        );

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Failed to fetch positions: " + response.body()
            );
        }

        Position[] positions =
                MAPPER.readValue(response.body(), Position[].class);

        return Arrays.stream(positions)
                .collect(Collectors.toMap(
                        Position::ticker,
                        p -> p
                ));
    }

    public AccountSummary getAccountSummary() throws Exception{
        HttpResponse<String> response = call(ServiceCallType.GET_ACCOUNT_SUMMARY, null, null);
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to fetch positions: " + response.body());
        }
        System.out.println(MAPPER.readValue(response.body(), AccountSummary.class));
        return MAPPER.readValue(response.body(), AccountSummary.class);
    }

    
    public double getFreeCash() throws Exception{
        return getAccountSummary().cash().availableToTrade();
    }

    
    public double getTotalValue() throws Exception {
        return getAccountSummary().totalValue();
    }

    
    public HttpResponse<String> buyMarket(String ticker, double quantity) throws Exception {
        return call(ServiceCallType.PLACE_MARKET_ORDER, null, new MarketRequest(ticker, quantity, false));
    }

    
    public HttpResponse<String> sellMarket(String ticker, double quantity) throws Exception {
        return buyMarket(ticker, -quantity);
    }
}