package sk.leo.api.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountSummary(
        String currency,
        double totalValue,
        Cash cash,
        Investments investments
) {
    public record Cash(
            double availableToTrade,
            double reservedForOrders,
            double inPies
    ) {}

    public record Investments(
            double currentValue,
            double totalCost,
            double realizedProfitLoss,
            double unrealizedProfitLoss
    ) {}
}
