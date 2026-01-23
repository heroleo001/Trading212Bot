package sk.leo.api;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

public record ServiceCall<RQ, RS> (
    ServiceCallType callType,
    Map<String, String> pathParams,
    RQ payload,
    TypeReference<RS> responseType,
    Consumer<RS> onResult
) {}
