package sk.leo.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.leo.api.records.ApiModels;

import java.io.InputStream;

public class ApiRegistryRead {

    private static final ApiModels.OpenApi API;

    static {
        try (InputStream is =
                     ApiRegistryRead.class.getResourceAsStream("/api.json")) {

            if (is == null) {
                throw new IllegalStateException("api.json not found");
            }

            ObjectMapper mapper = new ObjectMapper()
                    .configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false
                    );

            API = mapper.readValue(is, ApiModels.OpenApi.class);

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static {
        for (ServiceCallType key : ServiceCallType.values()) {
            EndpointResolver.resolve(key);
        }
    }

    public static ApiModels.OpenApi api() {
        return API;
    }
}
