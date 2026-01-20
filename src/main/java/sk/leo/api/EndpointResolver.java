package sk.leo.api;

import java.util.HashMap;
import java.util.Map;

public class EndpointResolver {

    public static ResolvedEndpoint resolve(EndpointKey key) {

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

                if (op != null && key.operationId().equals(op.operationId())) {

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
}
