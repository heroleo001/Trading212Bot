package sk.leo.api;

import com.fasterxml.jackson.core.type.TypeReference;
import sk.leo.api.records.EmptyRecord;
import sk.leo.api.records.Requests;

import java.util.function.Consumer;

public class ExtendedCommunicator extends LimitedCommunicator{
    public ExtendedCommunicator(String header) {
        super(header);
    }

    public void buyMarket(String ticker, double quantity, Consumer<Integer> responseCodeAction) throws Exception {
        ServiceCall<Requests.MarketRequest, EmptyRecord> call = new ServiceCall<>(
                ServiceCallType.PLACE_MARKET_ORDER,
                null,
                new Requests.MarketRequest(ticker, quantity, false),
                new TypeReference<EmptyRecord>() {},
                ignore -> {}
        );
    }


    public void sellMarket(String ticker, double quantity, Consumer<Integer> responseCodeAction) throws Exception {
        sellMarket(ticker, -quantity, responseCodeAction);
    }
}
