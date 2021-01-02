package com.company;

public class Main {

    public static void main(String[] args) {
        new Server((req, resp) -> {
            String body = "<html><body>" +
                    "<h1>Hello, naive server</h1> how are you?" +
                    "</body></html>";
            return body;
        }).bootStrap();
    }
}

