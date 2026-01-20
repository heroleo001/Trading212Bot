package sk.leo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import sk.leo.Endpoints;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Communicator {

    private static final String BASE_URL =
            "https://demo.trading212.com/api/v0";
    private final String header;

    private final HttpClient client = HttpClient.newHttpClient();
    private final Map<Endpoints, EndpointConfig> endpoints;

    public Communicator() {
        this.endpoints = loadEndpoints();
        header = Auth.header();
    }

    public HttpResponse<String> send(
            Endpoints endpoint,
            String body,
            String pathExtension
    ) throws Exception {
        EndpointConfig cfg = endpoints.get(endpoint);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + cfg.path() + pathExtension))
                .header("Authorization", header);

        //TODO add not all methods have bodies
        builder.header("Content-Type", "application/json");

        if (!Objects.isNull(body)){
            builder.method(cfg.method(), HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.method(cfg.method(), HttpRequest.BodyPublishers.noBody());
        }

        return client.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> send(Endpoints endpoint) throws Exception{
        return send(endpoint, null, null);
    }

    public HttpResponse<String> send(
            Endpoints endpoint,
            String body
    ) throws Exception{
        return send(endpoint, body, null);
    }

    // ===== helpers =====



    private Map<Endpoints, EndpointConfig> loadEndpoints() {
        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("endpoints.json")) {

            ObjectMapper mapper = new ObjectMapper();
            Map<String, EndpointConfig> raw =
                    mapper.readValue(is,
                            mapper.getTypeFactory().constructMapType(
                                    Map.class,
                                    String.class,
                                    EndpointConfig.class));

            Map<Endpoints, EndpointConfig> result =
                    new EnumMap<>(Endpoints.class);

            for (var entry : raw.entrySet()) {
                result.put(
                        Endpoints.valueOf(entry.getKey()),
                        entry.getValue()
                );
            }

            // fail fast
            for (Endpoints e : Endpoints.values()) {
                if (!result.containsKey(e)) {
                    throw new IllegalStateException(
                            "Missing endpoint config for " + e);
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load endpoints", e);
        }
    }

    public record EndpointConfig(
            String path,
            String method){}
}
