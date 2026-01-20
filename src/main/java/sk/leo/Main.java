package sk.leo;

import sk.leo.api.Communicator;
import sk.leo.api.EndpointKey;
import sk.leo.api.records.Requests;

public class Main {

    public static void main(String[] args) {

        try {
            Communicator.fetchPositions().forEach((key, value) -> System.out.println(value.name() + " value: " + value.currentValue()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}