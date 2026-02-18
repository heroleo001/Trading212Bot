package sk.leo.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import sk.leo.api.records.ResolvedEndpoint;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitedCommunicator {
    private final Map<ServiceCallType, Queue<ServiceCall<?, ?>>> queues = new ConcurrentHashMap<>();
    private static final Map<ServiceCallType, Deque<Instant>> calls = new ConcurrentHashMap<>();

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
    );
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static final AtomicInteger numberOfTDRequests = new AtomicInteger(0);

    private final String header;

    public RateLimitedCommunicator(String header) {
        this.header = header;
        Arrays.stream(ServiceCallType.values()).forEach(callType -> {
            calls.put(callType, new ConcurrentLinkedDeque<>());
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
        /// Temp solution to limit TD calls
        if (call.callType().getProvider() == ServiceCallType.Provider.TWELVE_DATA &&
                numberOfTDRequests.get() >= 800)
            return false;


        ServiceCallType callType = call.callType();
        Set<ServiceCallType> callTypes = getSetOfAllSharedLimitCalls(callType);

        Instant cutoffTime = Instant.now().minus(callType.getTimePeriod()); // Instant after which the calls are within the time period for the Call

        int callCounter = callTypes.stream()
                .mapToInt(t -> getCallAmountAfterCutoffTime(t, cutoffTime))
                .sum();

        return callCounter < callType.getOperationLimit();
    }

    private int getCallAmountAfterCutoffTime(ServiceCallType callType, Instant cutoffTime){
        Deque<Instant> recentCalls = calls.get(callType);
        recentCalls.removeIf(cutoffTime::isAfter);
        return recentCalls.size();
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

        /// Temp increment of TD call counter
        if (call.callType().getProvider() == ServiceCallType.Provider.TWELVE_DATA && call.callType().getOperationLimit() != Double.POSITIVE_INFINITY)
            numberOfTDRequests.incrementAndGet();

        int statusCode = httpResponse.statusCode();
        System.out.println("\n" + call.callType().name() + "\nStatus Code ______________________________________" + statusCode);

        if (httpResponse.statusCode() != 200) {
            System.out.println("Error\nCall Failed");
            call.onError().run();
        } else {
            RS responseObj = MAPPER.readValue(httpResponse.body(), call.responseType());
            call.onResult().accept(responseObj, httpResponse.body());
        }
    }

    private Set<ServiceCallType> getSetOfAllSharedLimitCalls(ServiceCallType callType){
        RateLimitGroup limitGroup = callType.getRateLimitGroup();
        if (limitGroup == null){
            return EnumSet.of(callType);
        } else {
            return limitGroup.getCallTypes();
        }
    }
}
