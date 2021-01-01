package com.company;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

    public static void main(String[] args) {
        new Server().bootStrap();
    }
}

class Server {
    private final static int BUFFER_SIZE = 256;
    private AsynchronousServerSocketChannel server;

    private final static String HEADERS =
            "HTTP/1.1 200 OK\n" +
                    "Server: naive\n" +
                    "Content-Type: text/html\n" +
                    "Content-Length: %s\n" +
                    "Connection: close\n\n";

    public void bootStrap() {

        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress("127.0.0.1", 8888));

            Future<AsynchronousSocketChannel> future = server.accept();

            System.out.println("new client thread");

            AsynchronousSocketChannel socketChannel = future.get(30, TimeUnit.SECONDS);

            while (socketChannel != null && socketChannel.isOpen()) {
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                StringBuilder stringBuilder = new StringBuilder();
                boolean keepReading = true;

                while (keepReading) {
                    socketChannel.read(buffer).get();

                    int position = buffer.position();

                    keepReading = position == BUFFER_SIZE;

                    byte[] array = keepReading ? buffer.array() : Arrays.copyOfRange(buffer.array(), 0, position);

                    stringBuilder.append(new String(array));
                    buffer.clear();
                }

                String body = "<html><body><h1>Hello, naive</h1></body></html>";
                String page = String.format(HEADERS, body.length()) + body;

                ByteBuffer response = ByteBuffer.wrap(page.getBytes());

                socketChannel.write(response);
                socketChannel.close();
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
}
