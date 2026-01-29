package sk.leo.api;

import sk.leo.api.records.ApiModels;
import sk.leo.api.records.ResolvedEndpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EndpointResolver {

    private static final Map<ServiceCallType, ResolvedEndpoint> twelveDataServiceCalls =
            Map.of(ServiceCallType.EXCHANGE_RATE, new ResolvedEndpoint("get", "exchange_rate", null),
                    ServiceCallType.SYMBOL_SEARCH, new ResolvedEndpoint("get", "symbol_search", null),
                    ServiceCallType.TIME_SERIES, new ResolvedEndpoint("get", "time_series", null)
            );

    public static ResolvedEndpoint resolve(ServiceCallType key) {
        if (twelveDataServiceCalls.containsKey(key)){
            return twelveDataServiceCalls.get(key);
        }

        for (var pathEntry : ApiRegistryRead.api().paths().entrySet()) {

            String path = pathEntry.getKey();
            ApiModels.PathItem item = pathEntry.getValue();

            Map<String, ApiModels.Operation> ops = new HashMap<>();
            if (item.get() != null) ops.put("GET", item.get());
            if (item.post() != null) ops.put("POST", item.post());
            if (item.put() != null) ops.put("PUT", item.put());
            if (item.delete() != null) ops.put("DELETE", item.delete());

            for (var entry : ops.entrySet()) {
                String method = entry.getKey();
                ApiModels.Operation op = entry.getValue();

                if (op != null && normalizeOpId(key.operationId()).equals(op.operationId())) {

                    String schemaRef = null;
                    if (op.requestBody() != null) {
                        var mt = op.requestBody()
                                .content()
                                .get("application/json");
                        if (mt != null && mt.schema() != null) {
                            schemaRef = mt.schema().ref();
                        }
                    }

                    return new ResolvedEndpoint(
                            method,
                            path,
                            schemaRef
                    );
                }
            }
        }

        throw new IllegalStateException(
                "Operation not found in OpenAPI: " + key.operationId()
        );
    }

    private static String normalizeOpId(String s) {
        return s == null ? null : s.replaceAll("\\s+", "");
    }

}
