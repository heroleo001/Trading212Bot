package sk.leo.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ApiModels {

    public record OpenApi(
            Map<String, PathItem> paths
    ) {}

    public record PathItem(
            Operation get,
            Operation post,
            Operation put,
            Operation delete
    ) {}

    public record Operation(
            String operationId,
            RequestBody requestBody
    ) {}

    public record RequestBody(
            Map<String, MediaType> content
    ) {}

    public record MediaType(
            Schema schema
    ) {}

    public record Schema(
            @JsonProperty("$ref")
            String ref
    ) {}

}
