package sk.leo.api;

import sk.leo.api.records.AccountSummary;
import sk.leo.api.records.Instrument;
import sk.leo.api.records.Position;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ExtendedDataService extends DataService{
    public ExtendedDataService(String header, CountDownLatch readyLatch) {
        super(header, readyLatch);
    }

    public AccountSummary getAccountSummary(){
        return get(ServiceCallType.GET_ACCOUNT_SUMMARY, AccountSummary.class);
    }

    public List<Position> getOpenPositions(){
        Position[] positions = get(ServiceCallType.GET_OPEN_POSITIONS, Position[].class);
        return List.of(positions);
    }

    public List<Instrument> getAllAvailableInstruments(){
        Instrument[] instruments = get(ServiceCallType.GET_ALL_AVAILABLE_INSTRUMENTS, Instrument[].class);
        return List.of(instruments);
    }

    public List<Instrument> getAllAvailableInstruments(String type){ // Only Stocks, ETF, Crypto...
        return getAllAvailableInstruments().stream()
                .filter(instrument ->
                        instrument.type().equalsIgnoreCase(type)).toList();
    }
}
