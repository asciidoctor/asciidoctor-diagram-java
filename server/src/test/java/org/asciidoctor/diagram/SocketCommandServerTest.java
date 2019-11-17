package org.asciidoctor.diagram;

import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SocketCommandServerTest {
    @Test
    public void testHTTPMessage() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        TestGenerator generator = new TestGenerator();
        Map<String, DiagramGenerator> generators = new HashMap<>();
        generators.put(generator.getName(), generator);
        final SocketCommandServer server = new SocketCommandServer(serverSocket, generators);

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.processRequests();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        serverThread.start();

        Socket socket = new Socket(InetAddress.getLocalHost(), serverSocket.getLocalPort());
        HTTPOutputStream httpOut = new HTTPOutputStream(socket.getOutputStream());


        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);
        h.putValue(HTTPHeader.HOST, "localhost:" + serverSocket.getLocalPort());

        httpOut.writeRequest(new Request(
                URI.create("/" + generator.getName()),
                h,
                new byte[0]
        ));

        HTTPInputStream httpIn = new HTTPInputStream(socket.getInputStream());
        Response response = httpIn.readResponse();

        server.terminate();

        assertEquals(200, response.code);
        assertEquals(MimeType.PNG, response.headers.getValue(HTTPHeader.CONTENT_TYPE));
    }

    @Test
    public void testHTTPError() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        TestGenerator generator = new TestGenerator();
        Map<String, DiagramGenerator> generators = new HashMap<>();
        generators.put(generator.getName(), generator);
        final SocketCommandServer server = new SocketCommandServer(serverSocket, generators);

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.processRequests();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        serverThread.start();

        Socket socket = new Socket(InetAddress.getLocalHost(), serverSocket.getLocalPort());
        HTTPOutputStream httpOut = new HTTPOutputStream(socket.getOutputStream());


        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);
        h.putValue(HTTPHeader.HOST, "localhost:" + serverSocket.getLocalPort());

        httpOut.writeRequest(new Request(
                URI.create("/foobar"),
                h,
                new byte[0]
        ));

        HTTPInputStream httpIn = new HTTPInputStream(socket.getInputStream());
        Response response = httpIn.readResponse();

        server.terminate();

        assertEquals(404, response.code);
        assertEquals(MimeType.JSON_UTF8, response.headers.getValue(HTTPHeader.CONTENT_TYPE));
    }

    private static class TestGenerator implements DiagramGenerator
    {
        @Override
        public String getName()
        {
            return "test";
        }

        @Override
        public ResponseData generate(Request request) throws IOException
        {
            return new ResponseData(MimeType.PNG, new byte[0]);
        }
    }
}
