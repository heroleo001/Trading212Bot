package sk.leo.api;

public record ResolvedEndpoint(
    String method,
    String path,
    String schemaRef
) {}