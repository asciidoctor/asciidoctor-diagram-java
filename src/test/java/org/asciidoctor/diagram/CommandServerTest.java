package org.asciidoctor.diagram;

import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class CommandServerTest {
    @Test
    public void testHTTPMessage() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        final CommandServer server = new CommandServer(serverSocket);

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
                URI.create("/ditaa"),
                h,
                DitaaTest.DITAA_INPUT.getBytes(Charset.forName("UTF-8"))
        ));

        HTTPInputStream httpIn = new HTTPInputStream(socket.getInputStream());
        Response response = httpIn.readResponse();

        server.terminate();

        assertEquals(200, response.code);
        assertEquals(MimeType.PNG, response.headers.getValue(HTTPHeader.CONTENT_TYPE));
    }
}
