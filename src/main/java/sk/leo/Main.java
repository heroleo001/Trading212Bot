package sk.leo;

import sk.leo.api.*;
import sk.leo.api.DataService;
import sk.leo.api.records.Instrument;
import sk.leo.api.records.Position;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        DataService service = new DataService(Auth.header(), latch);

        latch.await();
        System.out.println("All data loaded!");
    }
}