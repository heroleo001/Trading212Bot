package sk.leo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sk.leo.api.Communicator;
import sk.leo.api.EndpointKey;
import sk.leo.api.Requests;

import java.net.http.HttpResponse;

public class Main {

    public static void main(String[] args) {

        var body = new Requests.MarketRequest(
                "RRl_EQ",
                -1,
                false);

        try {
            Communicator.call(EndpointKey.PLACE_MARKET_ORDER, null, body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}