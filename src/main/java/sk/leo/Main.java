package sk.leo;

import sk.leo.logic.PercentToMove;
import sk.leo.logic.TradingStrategy;


public class Main {
    public static void main(String[] args) {
        TradingStrategy strategy = new PercentToMove(
                0.04,
                0.10,
                -0.04,
                0.06);
        strategy.runDailyAnalysis();
    }
}