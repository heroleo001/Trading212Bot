package sk.leo.api.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Position (
        BaseCredentials instrument,
        double quantity,
        Double averagePrice,
        Double currentPrice,
        Double unrealizedPnl
) {
    public String ticker(){
        return instrument != null ? instrument.ticker() : null;
    }

    public String name(){
        return instrument != null ? instrument.name() : null;
    }

    public double currentValue(){
        return currentPrice != null ? quantity * currentPrice : 0.0;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BaseCredentials(
            String ticker,
            String name
    ) {}
}
