package sk.leo.api;

import sk.leo.api.records.AccountSummary;
import sk.leo.api.records.Instrument;
import sk.leo.api.records.Position;
import sk.leo.logic.TradeHelper;
import sk.leo.logic.local.LocalStorer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


public class ExtendedDataService extends DataService {
    public ExtendedDataService(String header) {
        super(header);
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

    public void storeTickerSymbolMapping() {
        Map<String, Instrument> validInst = getAllValidInstruments();
        CountDownLatch latch = new CountDownLatch(validInst.size());
        validInst.values().forEach(
                instrument -> getCommunicator().resolveSymbolByIsin(instrument.isin(),
                        (empty, body) -> {
                            Optional<String> symbol = ExtendedCommunicator.parseSymbolFromResponseBody(body);
                            symbol.ifPresent(s -> LocalStorer.addToTickerSymbolMapping(instrument.ticker(), s));
                            latch.countDown();
                        }));
        try {
            latch.await();
            LocalStorer.storeMappingToFile();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Position> getPosition(String ticker){
        Map<String, Position> positionMap = getOpenPositions().stream().collect(Collectors.toMap(Position::ticker, p -> p));
        return Optional.ofNullable(positionMap.get(ticker));
    }
}
