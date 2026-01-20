package sk.leo.api.records;

public record Position (
        Instrument instrument,
        double quantity,
        double averagePrice,
        double currentPrice,
        double unrealizedPnl
) {
    public String ticker(){
        return instrument != null ? instrument.ticker() : null;
    }

    public String name(){
        return instrument != null ? instrument.name() : null;
    }

    public double currentValue(){
        return quantity * currentPrice;
    }
}
