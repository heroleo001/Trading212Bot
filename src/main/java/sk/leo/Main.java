package sk.leo;

import sk.leo.api.*;
import sk.leo.api.querying.DataService;

public class Main {
    public static void main(String[] args) {
        DataService SERVICE = new DataService(Auth.header());
    }
}