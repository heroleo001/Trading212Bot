package sk.leo.logic;

import sk.leo.api.Auth;
import sk.leo.api.ExtendedDataService;
import sk.leo.api.TwelveData.TwelveDataFetcher;
import sk.leo.api.records.Instrument;
import sk.leo.api.records.Position;
import sk.leo.api.records.RelevantStockData;
import sk.leo.logic.local.LocalStorer;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PercentToMove implements TradingStrategy {
    private final ExtendedDataService dataService;

    public final double appreciationLimit;
    public final double depreciationLimit;
    public final double onePositionPercentageLimit;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public PercentToMove(double appreciationLimit, double depreciationLimit, double onePositionPercentageLimit) {
        this.appreciationLimit = appreciationLimit;
        this.depreciationLimit = depreciationLimit;
        this.onePositionPercentageLimit = onePositionPercentageLimit;
        CountDownLatch latch = new CountDownLatch(1);

        dataService = new ExtendedDataService(Auth.header(), latch);

        /// Assure all data is loaded before moving on
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("All data loaded!");

        dataService.storeInstrumentMapping();
    }

    @Override
    public int runDailyAnalysis() {
        checkAndSellPositions();    //Buy
        checkAndBuyInstruments();   //Sell

        return 0;
    }

    private void checkAndBuyInstruments() {
        getBuyQuantities().forEach((ticker, quantity) -> {
//            dataService.getCommunicator().buyMarket(ticker, quantity);
            System.out.println("Would buy " + quantity + " " + ticker);
        });
    }

    private Map<String, Double> getBuyQuantities() {
        Map<String, RelevantStockData> instrumentsToBuy = getInstrumentsToBuy();

        Map<String, Double> exchangeRateToEur = Set.of("USD", "GBX").stream().collect(Collectors.toMap(
                p -> p,
                otherCurrency -> {
                    try {
                        return dataService.getTwelveDataFetcher().getExchangeRateToEur(otherCurrency).orElse(0.0);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }));

        double cashAvailable = dataService.getAccountSummary().cash().availableToTrade();
        double cashPerAsset = TradeHelper.round2(cashAvailable / instrumentsToBuy.size());

        if (cashPerAsset / cashAvailable > onePositionPercentageLimit) {
            cashPerAsset = onePositionPercentageLimit * cashAvailable;
        }

        final double finalCashPerAsset = cashPerAsset;

        return instrumentsToBuy.keySet().stream().collect(
                Collectors.toMap(
                        ticker -> ticker,
                        ticker -> {
                            RelevantStockData stockData = instrumentsToBuy.get(ticker);
                            return finalCashPerAsset / stockData.stockPrice() * exchangeRateToEur.get(stockData.currency());
                        })
        );
    }

    /**
     *
     * @return All the valid instruments with depreciation > limit
     */
    private Map<String, RelevantStockData> getInstrumentsToBuy() {
        Map<String, Instrument> validInstruments = dataService.getAllValidInstruments();
        System.out.println("There are " + validInstruments.size() + " tradable stocks:");

        TwelveDataFetcher twelveDataFetcher = dataService.getTwelveDataFetcher();
        Map<String, String> tickerSymbolMap = LocalStorer.loadTickerSymbolMapping();

        Map<String, RelevantStockData> result = new HashMap<>();

        validInstruments.values().forEach(instrument -> {
            try {
                Optional<RelevantStockData> optionalStockData = twelveDataFetcher.fetchRelevantStockData(
                        tickerSymbolMap.get(instrument.ticker()));
                RelevantStockData stockData;
                if (optionalStockData.isEmpty()) {
                    return;
                } else {
                    stockData = optionalStockData.get();
                }

                if (stockData.percentualChange() < depreciationLimit) {
                    result.put(instrument.ticker(), stockData);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return result;
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
