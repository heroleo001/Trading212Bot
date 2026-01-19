package sk.leo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.http.HttpResponse;

public class Main {

    private static final String API_KEY = System.getenv("T212_API_KEY");
    private static final String API_SECRET = System.getenv("T212_API_SECRET");

    private static final String BASE_URL =
            "https://demo.trading212.com/api/v0";

    public static void main(String[] args) {
        Communicator com = new Communicator(API_KEY, API_SECRET);

        try {
            HttpResponse<String> response = com.send(Endpoints.CANCEL_ORDER_BY_ID, null, "43200946852");
            System.out.println(response.body());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public static String getMarketOrderJson(){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        root.put("extendedHours", false);
        root.put("quantity", 20);
        root.put("ticker", "RRl_EQ");

        try {
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}