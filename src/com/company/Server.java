package com.company;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class Server {
    private final static int BUFFER_SIZE = 256;
    private AsynchronousServerSocketChannel server;

    private final HttpHanlder hanlder;

    Server(HttpHanlder hanlder) {
        this.hanlder = hanlder;
    }

    public void bootStrap() {

        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress("127.0.0.1", 8888));

            while (true) {
                handleSocket();
            }
        } catch (IOException e) {

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    private void handleSocket() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        Future<AsynchronousSocketChannel> future = server.accept();
        System.out.println("new client connection");

        AsynchronousSocketChannel socketChannel = future.get(30, TimeUnit.HOURS);

        while (socketChannel != null && socketChannel.isOpen()) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            StringBuilder stringBuilder = new StringBuilder();
            boolean keepReading = true;

            while (keepReading) {
                Integer size = socketChannel.read(buffer).get();

                keepReading = size == BUFFER_SIZE;
                buffer.flip();

                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);

                stringBuilder.append(charBuffer);
                buffer.clear();
            }

            HttpRequest request = new HttpRequest(stringBuilder.toString());

            HttpResponse response = new HttpResponse();

            if (hanlder != null) {

                try {
                    String body = hanlder.handle(request, response);

                    if (body != null && !body.isBlank()) {
                        if (response.getHeaders().get("Content-Type") == null) {
                            response.addHeader("Content-Type", "text/html; charset=utf-8");
                        }
                        response.setBody(body);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatusCode(500);
                    response.setStatus("Internal server error");
                    response.addHeader("Content-Type", "text/html; charset=utf-8");

                    response.setBody("<html><body><h1>" +
                            "Something went wrong 😡" +
                            "</h1></body></html>");


                }
            } else {

                response.setStatusCode(404);
                response.setStatus("Not found");
                response.addHeader("Content-Type", "text/html; charset=utf-8");
                response.setBody("<html><body><h1>" +
                        "Resource not found" +
                        "</h1></body></html>");

            }
            socketChannel.write(ByteBuffer.wrap(response.getBytes()));
            socketChannel.close();
        }
    }
}
