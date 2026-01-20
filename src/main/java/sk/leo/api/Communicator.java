package sk.leo.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.leo.api.records.Position;
import sk.leo.api.records.ResolvedEndpoint;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public final class Communicator {

    private static final String BASE_URL =
            "https://demo.trading212.com";
    private static final String HEADER = Auth.header();

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
    );
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static HttpResponse<String> call(
            EndpointKey endpoint,
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
                .header("Authorization", HEADER)
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

    public static Map<String, Position> fetchPositions() throws Exception {

        HttpResponse<String> response = call(
                EndpointKey.GET_OPEN_POSITIONS,
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
}