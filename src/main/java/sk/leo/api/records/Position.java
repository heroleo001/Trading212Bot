package sk.leo.api.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Position(
        BaseCredentials instrument,
        double quantity,
        Double currentPrice,
        WalletImpact walletImpact
) {
    public String ticker() {
        return instrument.ticker();
    }

    public String name() {
        return instrument.name();
    }

    public String isin() {
        return instrument.isin();
    }

    public String currency() {
        return instrument.currency();
    }

    public Double totalCost(){
        return walletImpact().totalCost();
    }

    public Double currentValue(){
        return walletImpact().currentValue();
    }

    public Double unrealisedPL(){
        return walletImpact.unrealizedProfitLoss;
    }

    public double appreciation(){
        return unrealisedPL() / totalCost();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BaseCredentials(
            String ticker,
            String name,
            String isin,
            String currency
    ) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WalletImpact(
        Double totalCost,
        Double currentValue,
        Double unrealizedProfitLoss
    ) {}
}
