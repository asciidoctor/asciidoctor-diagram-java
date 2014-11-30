package org.asciidoctor.diagram;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * An simple, single client command server that accepts HTTP messages as input.
 */
public class CommandServer {
    private final ServerSocket serverSocket;
    private CommandProcessor processor;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        System.out.println(serverSocket.getLocalPort());
        System.out.flush();

        CommandServer server = new CommandServer(serverSocket);
        server.processRequests();
        server.terminate();
    }

    public CommandServer(ServerSocket socket) {
        this.serverSocket = socket;
        this.processor = new CommandProcessor();
    }

    public void processRequests() throws IOException {
        while (!serverSocket.isClosed()) {
            Socket client = serverSocket.accept();

            RequestInput input = new HTTPInputStream(client.getInputStream());
            ResponseOutput output = new HTTPOutputStream(client.getOutputStream());

            Request request;
            while ((request = input.readRequest()) != null) {
                output.writeResponse(processor.processRequest(request));
                if ("close".equals(request.headers.getValue(HTTPHeader.CONNECTION))) {
                    break;
                }
            }

            client.close();
        }
    }

    public void terminate() throws IOException {
        serverSocket.close();
    }
}
