package sk.leo;

import sk.leo.api.Auth;
import sk.leo.api.ExtendedCommunicator;
import sk.leo.api.TwelveData.TwelveDataFetcher;
import sk.leo.logic.PercentToMove;
import sk.leo.logic.TradingStrategy;


public class Main {
    public static void main(String[] args) {
        TradingStrategy strategy = new PercentToMove(0.04, -0.04, 0.05);
        strategy.runDailyAnalysis();
    }

    @Deprecated
    private static void testCall(){
        TwelveDataFetcher fetcher = new TwelveDataFetcher(Auth.getTdApiKey());
        ExtendedCommunicator communicator = new ExtendedCommunicator(Auth.header(), () -> null);
        //            fetcher.getExchangeRateToEur("USD");
        communicator.getExchangeRateToEur("USD", (exchangeRate, s) -> System.out.println(s));
    }
}