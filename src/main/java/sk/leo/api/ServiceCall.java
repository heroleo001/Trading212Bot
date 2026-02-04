package sk.leo.api;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record ServiceCall<RQ, RS> (
    ServiceCallType callType,
    Map<UrlParamType, String> pathParams,
    RQ payload,
    TypeReference<RS> responseType,
    BiConsumer<RS, String> onResult
) {}
