package sk.leo.api;

import sk.leo.api.TwelveData.TwelveDataFetcher;
import sk.leo.api.records.AccountSummary;
import sk.leo.api.records.Instrument;
import sk.leo.api.records.Position;
import sk.leo.logic.TradeHelper;
import sk.leo.logic.local.LocalStorer;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class ExtendedDataService extends DataService {
    public ExtendedDataService(String header, CountDownLatch readyLatch) {
        super(header, readyLatch);
    }

    public AccountSummary getAccountSummary() {
        return get(ServiceCallType.GET_ACCOUNT_SUMMARY, AccountSummary.class);
    }

    public List<Position> getOpenPositions() {
        Position[] positions = get(ServiceCallType.GET_OPEN_POSITIONS, Position[].class);
        return List.of(positions);
    }

    public Map<String, Instrument> getAllAvailableInstruments() {
        Instrument[] instruments = get(ServiceCallType.GET_ALL_AVAILABLE_INSTRUMENTS, Instrument[].class);
        return Arrays.stream(instruments)
                .collect(Collectors.toMap(
                        Instrument::ticker,
                        p -> p
                ));
    }

    public Map<String, Instrument> getAllValidInstruments() {
        return getAllAvailableInstruments().values().stream().filter(TradeHelper::isValidBaseStock)
                .collect(Collectors.toMap(Instrument::ticker, p -> p));
    }

    public void storeInstrumentMapping() {
        Map<String, String> tickerSymbolMap =
                getAllValidInstruments().values().stream().collect(Collectors.toMap(
                        Instrument::ticker,
                        instrument -> {
                            try {
                                Optional<String> symbol = twelveDataFetcher.resolveSymbolByIsin(instrument.isin());
                                return symbol.orElse("");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }));

        LocalStorer.storeTickerSymbolMapping(tickerSymbolMap);
    }
}
