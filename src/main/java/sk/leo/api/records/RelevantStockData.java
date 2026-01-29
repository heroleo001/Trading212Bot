package sk.leo.api.records;

public record RelevantStockData(
        Double percentualChange,
        double stockPrice,
        String currency
) {
}
