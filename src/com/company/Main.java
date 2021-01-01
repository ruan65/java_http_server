package com.company;

public class Main {

    public static void main(String[] args) {
        new Server((req, resp) -> {
            String body = "<html><body>" +
                    "<h1>Hello, naive</h1>" +
                    "</body></html>";
            return body;
        }).bootStrap();
    }
}

