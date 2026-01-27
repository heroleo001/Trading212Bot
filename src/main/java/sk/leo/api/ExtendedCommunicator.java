package sk.leo.api;

import com.fasterxml.jackson.core.type.TypeReference;
import sk.leo.api.records.EmptyRecord;
import sk.leo.api.records.Instrument;
import sk.leo.api.records.Position;
import sk.leo.api.records.Requests;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExtendedCommunicator extends LimitedCommunicator{
    private final Supplier<Instrument[]> instruments;

    public ExtendedCommunicator(String header, Supplier<Instrument[]> instruments) {
        super(header);
        this.instruments = instruments;
    }

    public void buyMarket(String ticker,
                          double quantity) {
        if (quantity == 0) {
            System.out.println("Selling zero");
            return;
        }

        ServiceCall<Requests.MarketRequest, EmptyRecord> call = new ServiceCall<>(
                ServiceCallType.PLACE_MARKET_ORDER,
                null,
                new Requests.MarketRequest(ticker, quantity, supportsExtendedHours(ticker)),
                new TypeReference<EmptyRecord>() {},
                ignore -> {}
        );
        callService(call);
    }


    public void sellMarket(String ticker,
                           double quantity) {
        buyMarket(ticker, -quantity);
    }

    public void sellPosition(Position toSell){
        System.out.println("Selling " + toSell.quantityAvailableForTrading() + " " + toSell.name() + " stock");

        sellMarket(toSell.ticker(), toSell.quantityAvailableForTrading());
    }

    private Map<String, Instrument> getInstrumentsAsMap(){
        return Arrays.stream(instruments.get())
                .collect(Collectors.toMap(
                        Instrument::ticker,
                        p -> p
                ));
    }

    private boolean supportsExtendedHours(String ticker){
        return getInstrumentsAsMap().get(ticker).extendedHours();
    }
}
