package sk.leo.api.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Instrument(
        String ticker,
        String type, //Stock, ETF or ...
        String currencyCode,
        String name,
        Boolean extendedHours
) {}
