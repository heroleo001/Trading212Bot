package sk.leo.api;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataService {
    private final ExtendedCommunicator communicator;

    private final Map<ServiceCallType, Object> data = new ConcurrentHashMap<>() {
    };

    public DataService(String header) {
        communicator = new ExtendedCommunicator(header);

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
    }

    public ExtendedCommunicator getCommunicator() {
        return communicator;
    }
}
