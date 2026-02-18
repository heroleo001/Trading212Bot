package sk.leo.logic;

import sk.leo.api.Auth;
import sk.leo.api.ExtendedCommunicator;
import sk.leo.api.ExtendedDataService;
import sk.leo.api.ServiceCallType;
import sk.leo.api.records.Instrument;
import sk.leo.api.records.Position;
import sk.leo.api.records.RelevantStockData;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PercentToMove implements TradingStrategy {
    private final ExtendedDataService dataService;

    public final double appreciationLimit;
    public final double extremeAppreciationLimit;
    public final double depreciationLimit;
    public final double onePositionPercentageLimit;

    private final Map<String, Instrument> validInstruments;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public PercentToMove(double appreciationLimit, double extremeAppreciationLimit, double depreciationLimit, double onePositionPercentageLimit) {
        this.appreciationLimit = appreciationLimit;
        this.extremeAppreciationLimit = extremeAppreciationLimit;
        this.depreciationLimit = depreciationLimit;
        this.onePositionPercentageLimit = onePositionPercentageLimit;

        dataService = new ExtendedDataService(Auth.header());

        System.out.println("All data loaded!");

        dataService.storeTickerSymbolMapping();
        validInstruments = dataService.getAllValidInstruments();
        dataService.setRefreshNeeded(ServiceCallType.EXCHANGE_RATE, false);
    }

    @Override
    public void runDailyAnalysis() {
        checkAndSellPositions();                                                        //Sell
        /// 2min delay
        scheduler.schedule(this::checkAndBuyInstruments, 20, TimeUnit.MINUTES);    //Buy
    }

    private void checkAndBuyInstruments() {
        dataService.setRefreshNeeded(ServiceCallType.EXCHANGE_RATE, true);

        Map<String, Integer> buyQuantities = getBuyQuantities();
        System.out.println("\nStarting buying process!!! Buying " + buyQuantities.size() + " stocks");
        for (var stock : buyQuantities.entrySet()) {
            dataService.getCommunicator().buyMarket(stock.getKey(), stock.getValue());
        }

        System.out.println("Finished Process!!!!!!!!!");
        dataService.setRefreshNeeded(ServiceCallType.EXCHANGE_RATE, false);
    }

    private Map<String, Integer> getBuyQuantities() {
        Map<String, RelevantStockData> instrumentsToBuy = getInstrumentsToBuy();
        if (instrumentsToBuy.isEmpty()) {
            System.out.println("Nothing to buy");
            return Map.of();
        }

        double cashAvailable = dataService.getAccountSummary().cash().availableToTrade();
        final double notLimitedCashPerAsset = TradeHelper.round2(cashAvailable / instrumentsToBuy.size());

        return instrumentsToBuy.entrySet().stream()
                .flatMap(entry ->
                        dataService.getExchangeRateToEur(entry.getValue().currency()).stream()
                                .map(exchangeRate -> {
                                    double valueToBuy = notLimitedCashPerAsset;
                                    final double maxBuyableValue = maxBuyableValueToKeepLimit(entry.getKey());
                                    if (valueToBuy > maxBuyableValue)
                                        valueToBuy = maxBuyableValue;
                                    System.out.print("Trying to map the buy order to");


                                    return Map.entry(
                                            entry.getKey(),
                                            (int) (valueToBuy
                                                    / entry.getValue().stockPrice()
                                                    * exchangeRate));
                                })
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private double maxBuyableValueToKeepLimit(String ticker) {
        final double currentValue = dataService.getPosition(ticker)
                .map(Position::currentValue)
                .orElse(0.0);
        final double totalAccountValue = dataService.getAccountSummary().totalValue();
        return totalAccountValue * onePositionPercentageLimit - currentValue;
    }

    /**
     *
     * @return All the valid instruments with depreciation > limit
     */
    private Map<String, RelevantStockData> getInstrumentsToBuy() {
        System.out.println("There are " + validInstruments.size() + " tradable stocks:");

        for (var ins : validInstruments.keySet())
            System.out.println(ins);

        Map<String, RelevantStockData> result = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(validInstruments.size());
        Set<String> toRemove = ConcurrentHashMap.newKeySet();

        for (var instrument : validInstruments.values()) {
            System.out.println("Name: " + instrument.name());
            dataService.getCommunicator().fetchRelevantStockDataByTicker(
                    instrument.ticker(),
                    (empty, body) -> {
                        try {
                            Optional<RelevantStockData> stockDataOptional =
                                    ExtendedCommunicator.parseStockDataFromResponseBody(body);
                            if (stockDataOptional.isPresent()) {
                                RelevantStockData data = stockDataOptional.get();
                                if (isIgnorable(stockDataOptional.get())) { /// Remove from list if the price is too low (Penny-stock)
                                    toRemove.add(instrument.ticker());

                                    /// Main Part
                                } else if (data.percentualChange() < depreciationLimit) {/// Checks weather the stock depreciated enough
                                    result.put(instrument.ticker(), data);
                                    System.out.println("Adding " + instrument.name() + " to buy list.\nIt depreciated: " + data.percentualChange() + "\nthe price is: " + data.stockPrice() + " in " + data.currency());
                                    System.out.println(instrument);
                                }
                            } else { /// Remove instrument from list if the TIME_SERIES req. returned error, probably because it is not accessible with your pay-grade
                                toRemove.add(instrument.ticker());
                            }
                        } finally {
                            countDownAndPrintProgress(latch);
                        }
                    },
                    /// OnError runnable. Ensures the process gets finished when the response returns an ERROR
                    () -> countDownAndPrintProgress(latch)
            );

        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("\n\n____________________Finished Searching for stocks to buy___________________________");
        }

        validInstruments.entrySet().removeIf(entry -> toRemove.contains(entry.getKey()));
        return result;
    }

    private void countDownAndPrintProgress(CountDownLatch latch) {
        latch.countDown();
        System.out.println("Remaining " + latch.getCount() + "/" + validInstruments.size());
        System.out.println("Estimated time > " + latch.getCount() / 8 + " minutes");
    }

    /**
     *
     * @return weather the stock is cheaper than 10€.   Probably a penny-stock
     *
     */
    private boolean isIgnorable(RelevantStockData data) {
        Optional<Double> exchangeRate = dataService.getExchangeRateToEur(data.currency());
        return exchangeRate.map(aDouble -> data.stockPrice() * aDouble
                < 10.0
        ).orElse(true);
    }

    @Override
    public void start() {
        long initialDelay = computeInitialDelay();
        long period = TimeUnit.DAYS.toSeconds(1);

        scheduler.scheduleAtFixedRate(() -> {
            DayOfWeek today = LocalDate.now(ZoneId.systemDefault()).getDayOfWeek();

            if (today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY) {
                System.out.println("Running analysis");
                runDailyAnalysis();
            }
        }, initialDelay, period, TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(this::checkExtremeAppreciation,
                0, dataService.getRefreshTimePeriodMin(), TimeUnit.MINUTES);
    }

    private void checkExtremeAppreciation() {
        dataService.getOpenPositions().stream().filter(
                position -> position.appreciation() >= extremeAppreciationLimit
        ).forEach(position ->
                scheduler.schedule(() -> dataService.getCommunicator().sellPosition(position),
                        20, TimeUnit.MINUTES));    ///Sell position after 20min
    }

    private static long computeInitialDelay() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime nextRun = now.withHour(14).withMinute(0).withSecond(0);

        if (!now.isBefore(nextRun))
            nextRun = nextRun.plusDays(1);

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
