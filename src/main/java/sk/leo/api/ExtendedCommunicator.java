package sk.leo.api;

import com.fasterxml.jackson.core.type.TypeReference;
import sk.leo.api.records.EmptyRecord;
import sk.leo.api.records.Requests;

import java.util.function.Consumer;

public class ExtendedCommunicator extends LimitedCommunicator{
    public ExtendedCommunicator(String header) {
        super(header);
    }

    public void buyMarket(String ticker,
                          double quantity,
                          boolean extendedHours) {
        ServiceCall<Requests.MarketRequest, EmptyRecord> call = new ServiceCall<>(
                ServiceCallType.PLACE_MARKET_ORDER,
                null,
                new Requests.MarketRequest(ticker, quantity, extendedHours),
                new TypeReference<EmptyRecord>() {},
                ignore -> {}
        );
        callService(call);
    }


    public void sellMarket(String ticker,
                           double quantity,
                           boolean extendedHours) {
        buyMarket(ticker, -quantity, extendedHours);
    }
}
