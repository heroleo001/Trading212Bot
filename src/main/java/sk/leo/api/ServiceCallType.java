package sk.leo.api;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

public enum ServiceCallType {

    GET_ACCOUNT_SUMMARY("getAccountSummary", 1, Duration.ofSeconds(5)),

    GET_OPEN_POSITIONS("getPositions", 1, Duration.ofSeconds(1)),
    GET_ALL_AVAILABLE_INSTRUMENTS("instruments", 1, Duration.ofSeconds(50)),

    PLACE_MARKET_ORDER("placeMarketOrder", 50, Duration.ofSeconds(60)),
    PLACE_LIMIT_ORDER("placeLimitOrder", 1, Duration.ofSeconds(2)),
    PLACE_STOP_ORDER("placeStopOrder_1", 1, Duration.ofSeconds(2)),
    PLACE_STOP_LIMIT_ORDER("placeStopOrder", 1, Duration.ofSeconds(2)),

    GET_ORDER_BY_ID("orderById", 1, Duration.ofSeconds(1)),
    CANCEL_ORDER("cancelOrder", 50, Duration.ofSeconds(60));

    private final String operationId;
    private final int operationLimit;
    private final Duration timePeriod;

    ServiceCallType(String operationId, int operationLimit, Duration timePeriod1) {
        this.operationId = operationId;
        this.operationLimit = operationLimit;
        this.timePeriod = timePeriod1;
    }

    public String operationId() {
        return operationId;
    }

    public Duration getTimePeriod() {
        return timePeriod;
    }

    public int getOperationLimit() {
        return operationLimit;
    }
}
