package sk.leo;

import sk.leo.api.Communicator;

public class Main {

    public static void main(String[] args) {

        try {
            System.out.println(Communicator.getTotalValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}