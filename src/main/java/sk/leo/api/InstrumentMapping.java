package sk.leo.api;

public record InstrumentMapping(
        String isin,
        String t212Ticker,
        String externalSymbol
) { }
