package sk.leo.api.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CurrencyExchangeRate(
        Double rate
) {
}
