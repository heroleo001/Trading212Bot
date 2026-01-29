package sk.leo;

import sk.leo.api.*;
import sk.leo.logic.PercentToMove;
import sk.leo.logic.TradingStrategy;

import java.io.IOException;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {

        TradingStrategy strategy = new PercentToMove(0.04, -0.04, 0.05);
        strategy.runDailyAnalysis();



//        testCall();
    }

    @Deprecated
    private static void testCall(){
        Communicator communicator = new Communicator(Auth.header());
        try {
            HttpResponse<String> res = communicator.call(ServiceCallType.GET_OPEN_POSITIONS, null, null);
            System.out.println(res.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}