package sk.leo.logic;

import sk.leo.api.Auth;
import sk.leo.api.ExtendedCommunicator;
import sk.leo.api.ExtendedDataService;
import sk.leo.api.records.Instrument;
import sk.leo.api.records.Position;
import sk.leo.api.records.RelevantStockData;
import sk.leo.logic.local.LocalStorer;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PercentToMove implements TradingStrategy {
    private final ExtendedDataService dataService;

    public final double appreciationLimit;
    public final double depreciationLimit;
    public final double onePositionPercentageLimit;

    private final Map<String, Instrument> validInstruments;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public PercentToMove(double appreciationLimit, double depreciationLimit, double onePositionPercentageLimit) {
        this.appreciationLimit = appreciationLimit;
        this.depreciationLimit = depreciationLimit;
        this.onePositionPercentageLimit = onePositionPercentageLimit;

        dataService = new ExtendedDataService(Auth.header());

        System.out.println("All data loaded!");

        dataService.storeInstrumentSymbolMapping();
        validInstruments = dataService.getAllValidInstruments();
    }

    @Override
    public int runDailyAnalysis() {
//        checkAndSellPositions();                                                        //Sell
        /// 2min delay
//        scheduler.schedule(this::checkAndBuyInstruments, 2, TimeUnit.MINUTES);    //Buy
        System.out.println("dataService.getExchangeRateToEur(\"GBP\") = " + dataService.getExchangeRateToEur("GBP"));
        System.out.println("dataService.getExchangeRateToEur(\"GBX\") = " + dataService.getExchangeRateToEur("GBX"));
        return 0;
    }

    private void checkAndBuyInstruments() {
        Map<String, Integer> buyQuantities = getBuyQuantities();
        System.out.println("\nStarting buying process!!!");
        for (var stock : buyQuantities.entrySet()) {
            dataService.getCommunicator().buyMarket(stock.getKey(), stock.getValue());
        }
    }

    private Map<String, Integer> getBuyQuantities() {
        Map<String, RelevantStockData> instrumentsToBuy = getInstrumentsToBuy();

        double cashAvailable = dataService.getAccountSummary().cash().availableToTrade();
        double cashPerAsset = TradeHelper.round2(cashAvailable / instrumentsToBuy.size());

        if (cashPerAsset / cashAvailable > onePositionPercentageLimit) {
            cashPerAsset = onePositionPercentageLimit * cashAvailable;
        }

        final double finalCashPerAsset = cashPerAsset;

        return instrumentsToBuy.entrySet().stream()
                .flatMap(entry ->
                        dataService.getExchangeRateToEur(entry.getValue().currency()).stream()
                                .map(exchangeRate -> Map.entry(
                                        entry.getKey(),
                                        (int) (finalCashPerAsset
                                                / entry.getValue().stockPrice()
                                                * exchangeRate)
                                ))
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

    }

    /**
     *
     * @return All the valid instruments with depreciation > limit
     */
    private Map<String, RelevantStockData> getInstrumentsToBuy() {
        System.out.println("There are " + validInstruments.size() + " tradable stocks:");

        Map<String, String> tickerSymbolMap = LocalStorer.loadTickerSymbolMapping();

        Map<String, RelevantStockData> result = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(validInstruments.size());
        Set<String> toRemove = ConcurrentHashMap.newKeySet();

        for (var instrument : validInstruments.values()) {
            String symbol = tickerSymbolMap.get(instrument.ticker());
            if (symbol == null) {
                toRemove.add(instrument.ticker());
                latch.countDown();
                continue;
            }

            dataService.getCommunicator().fetchRelevantStockData(
                    symbol,
                    (empty, body) -> {
                        try {
                            ExtendedCommunicator
                                    .parseStockDataFromResponseBody(body)
                                    .ifPresent(data -> {
                                        System.out.println("Checking validity for: " + instrument.name());
                                        if (isIgnorable(data)) {
                                            toRemove.add(instrument.ticker());
                                            return;
                                        }
                                        if (data.percentualChange() < depreciationLimit) {/// Checks weather the stock depreciated enough
                                            result.put(instrument.ticker(), data);
                                            System.out.println("Adding " + instrument.name() + " to buy list.\nIt depreciated: " + data.percentualChange() + "\nthe price is: " + data.stockPrice() + " in " + data.currency());
                                            System.out.println(instrument);
                                        }
                                    });
                        } finally {
                            latch.countDown(); // ALWAYS
                        }
                    }
            );
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        validInstruments.entrySet().removeIf(entry -> toRemove.contains(entry.getKey()));

        return result;
    }

    private boolean isIgnorable(RelevantStockData data) {
        return data.stockPrice() * dataService.getExchangeRateToEur(data.currency())
                <
                10.0;
    }

    @Override
    public void start() {
        long initialDelay = computeInitialDelay();
        long period = TimeUnit.DAYS.toSeconds(1);

        scheduler.scheduleAtFixedRate(() -> {
            DayOfWeek today = LocalDate.now(ZoneId.systemDefault()).getDayOfWeek();

            if (today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY) {
                System.out.println("Daily analysis return: " + runDailyAnalysis());
            }
        }, initialDelay, period, TimeUnit.SECONDS);
    }

    private static long computeInitialDelay() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime nextRun = now.withHour(15).withMinute(30).withSecond(0);

        if (!now.isBefore(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        return Duration.between(now, nextRun).getSeconds();
    }

    private void checkAndSellPositions() {
        List<Position> positions = dataService.getOpenPositions();

        /// Sell the positions that are up 3%
        positions.forEach(pos -> System.out.println(pos.name() + " is up: " + pos.appreciation()));

        positions.stream()
                .filter(position -> position.appreciation() > appreciationLimit)
                .forEach(dataService.getCommunicator()::sellPosition);
    }
}
