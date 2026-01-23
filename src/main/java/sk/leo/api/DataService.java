package sk.leo.api;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class DataService {
    private final ExtendedCommunicator communicator;

    private final Map<ServiceCallType, Object> data = new ConcurrentHashMap<>() {};

    private final CountDownLatch readyLatch;
    private final AtomicInteger remaining;


    public DataService(String header, CountDownLatch readyLatch) {
        communicator = new ExtendedCommunicator(header);

        this.readyLatch = readyLatch;

        long count = Arrays.stream(ServiceCallType.values())
                .filter(ServiceCallType::isToRefresh)
                .count();
        this.remaining = new AtomicInteger((int) count);


        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                rereadData();
            }
        },0, 1000 * 60 * 5);
    }

    public <T> T get(ServiceCallType type, Class<T> expected) {
        Object value = data.get(type);
        return expected.cast(value);
    }

    private void rereadData() {
        Arrays.stream(ServiceCallType.values()).
                filter(ServiceCallType::isToRefresh).forEach(type -> {
                    ServiceCall<?, ?> call = type.createRefreshCall(this);
                    communicator.callService(call);
                });
    }

    public <T> void put(ServiceCallType type, T value) {
        data.put(type, value);

        if (remaining.decrementAndGet() == 0){
            readyLatch.countDown();
        }
    }

    public ExtendedCommunicator getCommunicator() {
        return communicator;
    }
}
