package org.asciidoctor.diagram;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * An simple, single client command server that accepts HTTP messages as input.
 */
public class CommandServer {
    private static final Map<String, DiagramGenerator> DEFAULT_GENERATORS = loadGenerators();

    private final ServerSocket serverSocket;
    private CommandProcessor processor;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        System.out.println(serverSocket.getLocalPort());
        System.out.flush();

        CommandServer server = new CommandServer(serverSocket, getGenerators());
        server.processRequests();
        server.terminate();
    }

    public CommandServer(ServerSocket socket, Map<String, DiagramGenerator> generators) {
        this.serverSocket = socket;
        this.processor = new CommandProcessor(generators);
    }

    static Map<String, DiagramGenerator> getGenerators() {
        return DEFAULT_GENERATORS;
    }

    private static Map<String, DiagramGenerator> loadGenerators()
    {
        Map<String, DiagramGenerator> generatorMap = new HashMap<String, DiagramGenerator>();

        ServiceLoader<DiagramGenerator> generatorLoader = ServiceLoader.load(DiagramGenerator.class, CommandServer.class.getClassLoader());
        for (DiagramGenerator generator : generatorLoader) {
            generatorMap.put(generator.getName(), generator);
        }
        ServiceLoader<DiagramGenerator> generatorLoaderTCCL = ServiceLoader.load(DiagramGenerator.class);
        for (DiagramGenerator generator : generatorLoaderTCCL) {
            generatorMap.put(generator.getName(), generator);
        }
        return generatorMap;
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
