package sk.leo.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.leo.api.records.ResolvedEndpoint;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LimitedCommunicator {
    private final Map<ServiceCallType, Queue<ServiceCall<?, ?>>> queues = new ConcurrentHashMap<>();
    private final Map<ServiceCallType, Queue<Instant>> calls = new ConcurrentHashMap<>();

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
    );
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private final String header;

    public LimitedCommunicator(String header) {
        this.header = header;
        Arrays.stream(ServiceCallType.values()).forEach(callType -> {
            calls.put(callType, new ConcurrentLinkedQueue<>());
            queues.put(callType, new ConcurrentLinkedQueue<>());
        });

        new Timer().scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        handleQueue();
                    }
                },
                0,
                5000
        );
    }

    public void callService(ServiceCall<?, ?> call) {
        this.queues.get(call.callType()).add(call);
        handleQueue();
    }

    private boolean canCallNow(ServiceCall<?, ?> call) {
        ServiceCallType type = call.callType();
        Queue<Instant> q = calls.get(type);

        Instant cutoffTime = Instant.now().minus(type.getTimePeriod()); // Instant after which the calls are within the time period for the Call

        q.removeIf(cutoffTime::isAfter);

        return q.size() < type.getOperationLimit();
    }

    private <RS> void handleQueue() {
        queues.keySet().forEach(callType -> {
            @SuppressWarnings("unchecked")
            ServiceCall<?, RS> call = (ServiceCall<?, RS>) queues.get(callType).peek();
            if (call == null) return; // nothing to do

            if (canCallNow(call)) {
                try {
                    handleCallNow(Objects.requireNonNull(queues.get(callType).poll()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private <RQ, RS> void handleCallNow(ServiceCall<RQ, RS> call) throws Exception {
        ResolvedEndpoint ep = call.callType().getEndpoint();

//        ///  Adds path-params if necessary
//        if (call.pathParams() != null) {
//            for (var e : call.pathParams().entrySet()) {
//                endpointPath = endpointPath.replace("{" + e.getKey() + "}", e.getValue());
//            }
//        }
//
//
//        HttpRequest.Builder builder = HttpRequest.newBuilder()
//                .uri(URI.create(call.callType().getProvider().getBaseUrl() + "/" + endpointPath))
//                .header("Authorization", header)
//                .header("Content-Type", "application/json");
//
//
//        if (call.payload() != null) {
//            try {
//                builder.method(
//                        ep.method(),
//                        HttpRequest.BodyPublishers.ofString(
//                                MAPPER.writeValueAsString(call.payload())
//                        )
//                );
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        } else {
//            builder.method(
//                    ep.method(),
//                    HttpRequest.BodyPublishers.noBody()
//            );
//        }
        HttpRequest request = call.callType().createRequest(
                header,
                call.pathParams(),
                ep.method(),
                call.payload());

        /// Sending Request
        HttpResponse<String> httpResponse = CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
        calls.get(call.callType()).add(Instant.now());


        int statusCode = httpResponse.statusCode();
        System.out.println("\n" + call.callType().name() + "\nStatus Code ______________________________________" + statusCode);

        if (httpResponse.statusCode() != 200) {
            throw new Exception("Leo   Request failed");
        } else {
            RS responseObj = MAPPER.readValue(httpResponse.body(), call.responseType());
            call.onResult().accept(responseObj, httpResponse.body());
        }
    }
}
