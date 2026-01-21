package sk.leo.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.leo.api.records.ResolvedEndpoint;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class LimitedCommunicator {
    private static final String BASE_URL =
            "https://demo.trading212.com";

    private final Map<ServiceCallType, Queue<ServiceCall<?, ?>>> queues = new HashMap<>();
    private final Map<ServiceCallType, List<Instant>> calls = new HashMap<>();

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private final String header;

    public LimitedCommunicator(String header) {
        this.header = header;
        Arrays.stream(ServiceCallType.values()).forEach(callType -> {
            calls.put(callType, new ArrayList<>());
            queues.put(callType, new LinkedList<>());
        });
        new Timer().scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        handleQueue();
                    }
                },
                0,
                1000
        );
    }

    public void callService(ServiceCall<?, ?> call) {
        this.queues.get(call.callType()).add(call);
    }

    private boolean canCallNow(ServiceCall<?, ?> call) {
        ServiceCallType type = call.callType();
        int currentRateLimitCountDuringCurrentPeriod = (int) calls.get(type).stream()
                .filter(callTimestamp -> Instant.now()
                        .minus(type.getTimePeriod())
                        .isAfter(callTimestamp))
                .count();
        return currentRateLimitCountDuringCurrentPeriod < type.getOperationLimit();
    }

    private <RS> void handleQueue() {
        queues.keySet().forEach(callType -> {
            @SuppressWarnings("unchecked")
            ServiceCall<?, RS> call = (ServiceCall<?, RS>) queues.get(callType).peek();
            if (call == null) return; // nothing to do

            if (canCallNow(call)) {
                try {
                    handleCall(queues.get(callType).poll());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private <RQ, RS> void handleCall(ServiceCall<RQ, RS> call) throws IOException, InterruptedException {
        ResolvedEndpoint ep = EndpointResolver.resolve(call.callType());
        String endpointPath = ep.path();

        ///  Adds path-params if necessary
        if (call.pathParams() != null){
            for (var e : call.pathParams().entrySet()) {
                endpointPath = endpointPath.replace("{" + e.getKey() + "}", e.getValue());
            }
        }


        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpointPath))
                .header("Authorization", header)
                .header("Content-Type", "application/json");


        if (call.payload() != null) {
            try {
                builder.method(
                        ep.method(),
                        HttpRequest.BodyPublishers.ofString(
                                MAPPER.writeValueAsString(call.payload())
                        )
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            builder.method(
                    ep.method(),
                    HttpRequest.BodyPublishers.noBody()
            );
        }

        HttpResponse<String> result = CLIENT.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        RS response = MAPPER.readValue(result.body(), call.responseType());
        call.onResult().accept(response);
    }
}
