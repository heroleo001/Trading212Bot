package sk.leo;

import com.fasterxml.jackson.core.type.TypeReference;
import sk.leo.api.*;
import sk.leo.api.records.EmptyResponse;
import sk.leo.api.records.Requests;

public class Main {
    private static final LimitedCommunicator API_SERVICE = new LimitedCommunicator(Auth.header());

    public static void main(String[] args) {

        API_SERVICE.callService(new ServiceCall<Requests.MarketRequest, EmptyResponse>(
                ServiceCallType.PLACE_MARKET_ORDER,
                null,
                new Requests.MarketRequest("RRl_EQ", 1, false),
                new TypeReference<EmptyResponse>(){},
                ignored -> System.out.println("Order assigned")
        ));
    }
}