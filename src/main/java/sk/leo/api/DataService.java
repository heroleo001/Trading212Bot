package sk.leo.api;

import sk.leo.api.records.Instrument;
import sk.leo.logic.TradeHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class DataService {
    private final ExtendedCommunicator communicator;

    private final Map<ServiceCallType, Object> data = new ConcurrentHashMap<>() {};
    private final Map<String, Double> currencyExchangeRateToEur = new ConcurrentHashMap<>();

    private final CountDownLatch readyLatch;
    private final AtomicInteger remaining;
    private final Set<String> validForeignCurrencies;

    public DataService(String header) {
        communicator = new ExtendedCommunicator(
                header,
                () -> get(ServiceCallType.GET_ALL_AVAILABLE_INSTRUMENTS, Instrument[].class));
        this.readyLatch = new CountDownLatch(1);

        validForeignCurrencies = TradeHelper.getValidForeignCurrencies();

        long toReadCount = Arrays.stream(ServiceCallType.values())
                .filter(ServiceCallType::isToRefresh)
                .count()
                +
                validForeignCurrencies.size();
        this.remaining = new AtomicInteger((int) toReadCount);

        fetchAllAvailableInstruments();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                rereadData();
            }
        },0, 1000 * 60 * 30);

        try {
            readyLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T get(ServiceCallType type, Class<T> expected) {
        Object value = data.get(type);
        return expected.cast(value);
    }

    private void rereadData() {
        rereadRefreshData();
        rereadExchangeRates();
    }

    public <T> void put(ServiceCallType type, T value) {
        data.put(type, value);
        decrementRemainingInitialCalls();
    }

    private void decrementRemainingInitialCalls(){
        if (remaining.decrementAndGet() == 0){
            readyLatch.countDown();
        }
    }

    public void putCurrency(String currency, double exchangeValue){
        currencyExchangeRateToEur.put(currency, exchangeValue);
        decrementRemainingInitialCalls();
    }

    public Optional<Double> getExchangeRateToEur(String currency){
        currency = currency.toUpperCase();
        if (currency.equals("EUR")) return Optional.of(1.0);
        System.out.println("Looking up FX rate for: " + currency);
        System.out.println("Available currencies: " + currencyExchangeRateToEur.keySet());

        if (!currencyExchangeRateToEur.containsKey(currency)){
            return Optional.empty();
        }
        return Optional.of(currencyExchangeRateToEur.get(currency));
    }

    private void rereadExchangeRates(){
        validForeignCurrencies.forEach(currency -> communicator.getExchangeRateToEur(currency,
                ((exchangeRate, body) -> putCurrency(currency, exchangeRate.rate()))));
    }

    private void fetchAllAvailableInstruments(){
        ServiceCall<?, ?> call = ServiceCallType.GET_ALL_AVAILABLE_INSTRUMENTS.createRefreshCall(this);
        communicator.callService(call);
    }

    private void rereadRefreshData(){
        Arrays.stream(ServiceCallType.values()).
                filter(ServiceCallType::isToRefresh).forEach(type -> {
                    ServiceCall<?, ?> call = type.createRefreshCall(this);
                    communicator.callService(call);
                });
    }

    public ExtendedCommunicator getCommunicator() {
        return communicator;
    }
}
