package sk.leo.api.records;

public class Requests {
    // ----------------------------
    // MARKET ORDER
    // ----------------------------
    public record MarketRequest(
            String ticker,
            double quantity,
            Boolean extendedHours
    ) {}

    // ----------------------------
    // LIMIT ORDER
    // ----------------------------
    public record LimitRequest(
            String ticker,
            double quantity,
            double limitPrice
    ) {}

    // ----------------------------
    // STOP ORDER
    // ----------------------------
    public record StopRequest(
            String ticker,
            double quantity,
            double stopPrice
    ) {}

    // ----------------------------
    // STOP LIMIT ORDER
    // ----------------------------
    public record StopLimitRequest(
            String ticker,
            double quantity,
            double stopPrice,
            double limitPrice
    ) {}
}
