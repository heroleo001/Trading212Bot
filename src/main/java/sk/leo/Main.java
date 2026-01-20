package sk.leo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sk.leo.api.Communicator;

import java.net.http.HttpResponse;

public class Main {



    private static final String BASE_URL =
            "https://demo.trading212.com/api/v0";

    public static void main(String[] args) {
        Communicator com = new Communicator();

        try {
            HttpResponse<String> response = com.send(Endpoints.PLACE_MARKET_ORDER, getMarketOrderJson());
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