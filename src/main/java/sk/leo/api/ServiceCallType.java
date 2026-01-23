package sk.leo.api;

import com.fasterxml.jackson.core.type.TypeReference;
import sk.leo.api.records.AccountSummary;
import sk.leo.api.records.Instrument;
import sk.leo.api.records.Position;

import java.time.Duration;

public enum ServiceCallType {

    GET_ACCOUNT_SUMMARY("getAccountSummary", 1, Duration.ofSeconds(5), true){
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            return new ServiceCall<>(this, null, null,
                    new TypeReference<AccountSummary>() {}, rs -> service.put(this, rs));
        }
    },

    GET_OPEN_POSITIONS("getPositions", 1, Duration.ofSeconds(1), true) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            return new ServiceCall<>(this, null, null,
                    new TypeReference<Position[]>() {}, rs -> service.put(this, rs));
        }
    },
    GET_ALL_AVAILABLE_INSTRUMENTS("instruments", 1, Duration.ofSeconds(50), true) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            return new ServiceCall<>(this, null, null,
                    new TypeReference<Instrument[]>() {}, rs -> service.put(this, rs));
        }
    },

    PLACE_MARKET_ORDER("placeMarketOrder", 50, Duration.ofSeconds(60), false) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }
    },
    PLACE_LIMIT_ORDER("placeLimitOrder", 1, Duration.ofSeconds(2), false) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }
    },
    PLACE_STOP_ORDER("placeStopOrder_1", 1, Duration.ofSeconds(2), false) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }
    },
    PLACE_STOP_LIMIT_ORDER("place   StopOrder", 1, Duration.ofSeconds(2), false) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }
    },

    GET_ORDER_BY_ID("orderById", 1, Duration.ofSeconds(1), false) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this should not be refreshed");
        }
    },
    CANCEL_ORDER("cancelOrder", 50, Duration.ofSeconds(60), false) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }
    };




    private final String operationId;
    private final int operationLimit;
    private final Duration timePeriod;
    private final boolean refreshData;

    ServiceCallType(String operationId, int operationLimit, Duration timePeriod, boolean refreshData) {
        this.operationId = operationId;
        this.operationLimit = operationLimit;
        this.timePeriod = timePeriod;
        this.refreshData = refreshData;
    }



    public String operationId() {
        return operationId;
    }

    public Duration getTimePeriod() {
        return timePeriod;
    }

    public int getOperationLimit() {
        return operationLimit;
    }

    public boolean isToRefresh() {
        return refreshData;
    }

    public abstract ServiceCall<?, ?> createRefreshCall(DataService service);
}
