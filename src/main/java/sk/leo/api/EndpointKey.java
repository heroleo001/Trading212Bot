package sk.leo.api;

public enum EndpointKey {

    GET_ACCOUNT_SUMMARY("getAccountSummary"),

    GET_OPEN_POSITIONS("getPositions"),

    PLACE_MARKET_ORDER("placeMarketOrder"),
    PLACE_LIMIT_ORDER("placeLimitOrder"),
    PLACE_STOP_ORDER("placeStopOrder_1"),
    PLACE_STOP_LIMIT_ORDER("placeStopOrder"),

    GET_ORDER_BY_ID("orderById"),
    CANCEL_ORDER("cancelOrder");

    private final String operationId;

    EndpointKey(String operationId) {
        this.operationId = operationId;
    }

    public String operationId() {
        return operationId;
    }
}
