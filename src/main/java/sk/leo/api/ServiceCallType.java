package sk.leo.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.leo.api.records.AccountSummary;
import sk.leo.api.records.Instrument;
import sk.leo.api.records.Position;
import sk.leo.api.records.ResolvedEndpoint;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Map;

public enum ServiceCallType {
    GET_ACCOUNT_SUMMARY("getAccountSummary", 1, Duration.ofSeconds(5), true, Provider.TRADING_212) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            return new ServiceCall<>(this, null, null,
                    new TypeReference<AccountSummary>() {
                    }, (rs, body) -> service.put(this, rs));
        }

        @Override
        public HttpRequest createRequest(String authorization, Map<UrlParamType, String> params, String method, Object payload) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getPathWithParams(getProvider().getBaseUrl() + "/" + getEndpoint().path(), params)))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json");
            addMethod(builder, method, payload);
            return builder.build();
        }
    },

    GET_OPEN_POSITIONS("getPositions", 1, Duration.ofSeconds(1), true, Provider.TRADING_212) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            return new ServiceCall<>(this, null, null,
                    new TypeReference<Position[]>() {
                    }, (rs, body) -> service.put(this, rs));
        }

        @Override
        public HttpRequest createRequest(String authorization, Map<UrlParamType, String> params, String method, Object payload) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getPathWithParams(getProvider().getBaseUrl() + "/" + getEndpoint().path(), params)))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json");
            addMethod(builder, method, payload);
            return builder.build();
        }
    },
    GET_ALL_AVAILABLE_INSTRUMENTS("instruments", 1, Duration.ofSeconds(50), true, Provider.TRADING_212) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            return new ServiceCall<>(this, null, null,
                    new TypeReference<Instrument[]>() {
                    }, (rs, body) -> service.put(this, rs));
        }

        @Override
        public HttpRequest createRequest(String authorization, Map<UrlParamType, String> params, String method, Object payload) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getPathWithParams(getProvider().getBaseUrl() + "/" + getEndpoint().path(), params)))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json");
            addMethod(builder, method, payload);
            return builder.build();
        }
    },

    PLACE_MARKET_ORDER("placeMarketOrder", 50, Duration.ofSeconds(60), false, Provider.TRADING_212) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }

        @Override
        public HttpRequest createRequest(String authorization, Map<UrlParamType, String> params, String method, Object payload) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getPathWithParams(getProvider().getBaseUrl() + "/" + getEndpoint().path(), params)))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json");
            addMethod(builder, method, payload);
            return builder.build();
        }
    },
    PLACE_LIMIT_ORDER("placeLimitOrder", 1, Duration.ofSeconds(2), false, Provider.TRADING_212) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }

        @Override
        public HttpRequest createRequest(String authorization, Map<UrlParamType, String> params, String method, Object payload) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getPathWithParams(getProvider().getBaseUrl() + "/" + getEndpoint().path(), params)))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json");
            addMethod(builder, method, payload);
            return builder.build();
        }
    },
    PLACE_STOP_ORDER("placeStopOrder_1", 1, Duration.ofSeconds(2), false, Provider.TRADING_212) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }

        @Override
        public HttpRequest createRequest(String authorization, Map<UrlParamType, String> params, String method, Object payload) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getPathWithParams(getProvider().getBaseUrl() + "/" + getEndpoint().path(), params)))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json");
            addMethod(builder, method, payload);
            return builder.build();
        }
    },
    PLACE_STOP_LIMIT_ORDER("place   StopOrder", 1, Duration.ofSeconds(2), false, Provider.TRADING_212) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }

        @Override
        public HttpRequest createRequest(String authorization, Map<UrlParamType, String> params, String method, Object payload) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getPathWithParams(getProvider().getBaseUrl() + "/" + getEndpoint().path(), params)))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json");
            addMethod(builder, method, payload);
            return builder.build();
        }
    },

//    GET_ORDER_BY_ID("orderById", 1, Duration.ofSeconds(1), false, Provider.TRADING_212) {
//        @Override
//        public ServiceCall<?, ?> createRefreshCall(DataService service) {
//            throw new UnsupportedOperationException("this should not be refreshed");
//        }
//    },
//    CANCEL_ORDER("cancelOrder", 50, Duration.ofSeconds(60), false, Provider.TRADING_212) {
//        @Override
//        public ServiceCall<?, ?> createRefreshCall(DataService service) {
//            throw new UnsupportedOperationException("this does not have a response");
//        }
//    },


    TIME_SERIES("time_series", 8, Duration.ofSeconds(60), false, Provider.TWELVE_DATA) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }

        @Override
        public HttpRequest createRequest(String authorization, Map<UrlParamType, String> params, String method, Object payload) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getPathWithParams(getProvider().getBaseUrl() + "/" + getEndpoint().path(), params)));
            addMethod(builder, method, payload);
            return builder.build();
        }
    },
    EXCHANGE_RATE("exchange_rate", 8, Duration.ofSeconds(60), false, Provider.TWELVE_DATA) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }

        @Override
        public HttpRequest createRequest(String authorization, Map<UrlParamType, String> params, String method, Object payload) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getPathWithParams(getProvider().getBaseUrl() + "/" + getEndpoint().path(), params)));
            addMethod(builder, method, payload);
            return builder.build();
        }
    },
    SYMBOL_SEARCH("symbol_search", Double.POSITIVE_INFINITY, Duration.ZERO, false, Provider.TWELVE_DATA) {
        @Override
        public ServiceCall<?, ?> createRefreshCall(DataService service) {
            throw new UnsupportedOperationException("this does not have a response");
        }

        @Override
        public HttpRequest createRequest(String authorization, Map<UrlParamType, String> params, String method, Object payload) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getPathWithParams(getProvider().getBaseUrl() + "/" + getEndpoint().path(), params)));
            addMethod(builder, method, payload);
            return builder.build();
        }
    };

    private static void addMethod(HttpRequest.Builder builder, String method, Object payload) {
        try {
            if (payload != null) {
                builder.method(method,
                        HttpRequest.BodyPublishers.ofString(
                                MAPPER.writeValueAsString(payload)
                        ));
            } else {
                builder.method(method,
                        HttpRequest.BodyPublishers.noBody());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private final String operationId;
    private final double operationLimit;
    private final Duration timePeriod;
    private final boolean refreshData;
    private final Provider provider;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    ServiceCallType(String operationId, double operationLimit, Duration timePeriod, boolean refreshData, Provider provider) {
        this.operationId = operationId;
        this.operationLimit = operationLimit;
        this.timePeriod = timePeriod;
        this.refreshData = refreshData;
        this.provider = provider;
    }


    private static String getPathWithParams(String basePath, Map<UrlParamType, String> urlParams) {
        if (urlParams == null) return basePath;
        if (urlParams.isEmpty()) return basePath;

        StringBuilder basePathBuilder = new StringBuilder(basePath);
        basePathBuilder.append("?");
        for (var paramType : urlParams.keySet()) {
            basePathBuilder.append(paramType.getAsString(urlParams.get(paramType)));
            basePathBuilder.append("&");
        }
        return basePathBuilder.toString();

    }

    public String operationId() {
        return operationId;
    }

    public Duration getTimePeriod() {
        return timePeriod;
    }

    public double getOperationLimit() {
        return operationLimit;
    }

    public boolean isToRefresh() {
        return refreshData;
    }

    public Provider getProvider() {
        return provider;
    }

    public ResolvedEndpoint getEndpoint() {
        return EndpointResolver.resolve(this);
    }

    public abstract ServiceCall<?, ?> createRefreshCall(DataService service);

    public abstract HttpRequest createRequest(String authorization, Map<UrlParamType, String> params, String method, Object payload);


    public enum Provider {
        TRADING_212("https://demo.trading212.com"),
        TWELVE_DATA("https://api.twelvedata.com");

        private final String base_url;

        Provider(String base_url) {
            this.base_url = base_url;
        }

        public String getBaseUrl() {
            return base_url;
        }
    }
}
