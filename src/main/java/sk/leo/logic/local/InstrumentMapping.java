package sk.leo.logic.local;

public record InstrumentMapping(
        String isin,
        String t212Ticker,
        String externalSymbol
) { }
