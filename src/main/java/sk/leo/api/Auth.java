package sk.leo.api;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Auth {

    private static final String API_KEY = System.getenv("T212_API_KEY");
    private static final String API_SECRET = System.getenv("T212_API_SECRET");

    public static String header() {
        String creds = API_KEY + ":" + API_SECRET;
        return "Basic " + Base64.getEncoder()
                .encodeToString(creds.getBytes(StandardCharsets.UTF_8));
    }
}
