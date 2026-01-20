package sk.leo.api.records;

public record ResolvedEndpoint(
    String method,
    String path,
    String schemaRef
) {}