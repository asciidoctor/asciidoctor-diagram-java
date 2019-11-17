package org.asciidoctor.diagram;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * An simple, single client command server that accepts HTTP messages as input.
 */
public class SocketCommandServer extends AbstractCommandServer {
    private final ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        System.out.println(serverSocket.getLocalPort());
        System.out.flush();

        SocketCommandServer server = new SocketCommandServer(serverSocket, getGenerators());
        server.processRequests();
        server.terminate();
    }

    public SocketCommandServer(ServerSocket socket, Map<String, DiagramGenerator> generators) {
        super(generators);
        this.serverSocket = socket;
    }

    public void processRequests() throws IOException {
        while (!serverSocket.isClosed()) {
            Socket client = serverSocket.accept();
            processRequests(client.getInputStream(), client.getOutputStream());
            client.close();
        }
    }

    public void terminate() throws IOException {
        serverSocket.close();
    }
}
