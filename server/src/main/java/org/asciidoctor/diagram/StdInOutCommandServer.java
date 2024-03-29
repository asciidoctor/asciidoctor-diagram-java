package org.asciidoctor.diagram;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

/**
 * An simple, single client command server that accepts HTTP messages as input.
 */
public class StdInOutCommandServer extends AbstractCommandServer {
    public static void main(String[] args) throws IOException {
        StdInOutCommandServer server = new StdInOutCommandServer(getGenerators());
        server.processRequests();
    }

    public StdInOutCommandServer(Map<String, DiagramGenerator> generators) {
        super(generators);
    }

    public void processRequests() throws IOException {
        PrintStream stdOut = System.out;
        System.setOut(System.err);
        processRequests(System.in, stdOut);
    }
}
