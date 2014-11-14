package org.asciidoctor.diagram;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class CommandServer {
    private CommandProcessor processor;
    private RequestInput input;
    private ResponseOutput output;

    public static void main(String[] args) throws IOException {
        int port = -1;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-p")) {
                port = Integer.parseInt(args[++i]);
            }
        }

        CommandServer server = createNetworkServer(port);
        server.processRequests();
    }

    public static CommandServer createNetworkServer(int port) throws IOException {
        Socket socket = new Socket(InetAddress.getLocalHost(), port);
        HTTPInputStream in = new HTTPInputStream(socket.getInputStream());
        HTTPOutputStream out = new HTTPOutputStream(socket.getOutputStream());

        return new CommandServer(in, out, new CommandProcessor());
    }

    public CommandServer(RequestInput input, ResponseOutput output, CommandProcessor processor) {
        this.input = input;
        this.output = output;
        this.processor = processor;
    }

    public void processRequests() throws IOException {
        Request request;
        while ((request = input.readRequest()) != null) {
            output.writeResponse(processor.processRequest(request));
        }
    }

    public void terminate() throws IOException {
        input.close();
    }
}
