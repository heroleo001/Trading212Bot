package sk.leo.api.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Instrument(
        String ticker,
        String type, //Stock, ETF or ...
        int workingScheduleId,
        String isin,
        String currencyCode,
        String name,
        double maxOpenQuantity,
        Boolean extendedHours
) {}
