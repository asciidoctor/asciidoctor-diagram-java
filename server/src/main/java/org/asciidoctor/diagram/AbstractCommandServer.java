package org.asciidoctor.diagram;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * An simple, single client command server that accepts HTTP messages as input.
 */
public class AbstractCommandServer {
    private static final Map<String, DiagramGenerator> DEFAULT_GENERATORS = loadGenerators();

    private CommandProcessor processor;

    public AbstractCommandServer(Map<String, DiagramGenerator> generators) {
        this.processor = new CommandProcessor(generators);
    }

    protected static Map<String, DiagramGenerator> getGenerators() {
        return DEFAULT_GENERATORS;
    }

    private static Map<String, DiagramGenerator> loadGenerators()
    {
        Map<String, DiagramGenerator> generatorMap = new HashMap<String, DiagramGenerator>();

        ServiceLoader<DiagramGenerator> generatorLoader = ServiceLoader.load(DiagramGenerator.class, AbstractCommandServer.class.getClassLoader());
        for (DiagramGenerator generator : generatorLoader) {
            generatorMap.put(generator.getName(), generator);
        }
        ServiceLoader<DiagramGenerator> generatorLoaderTCCL = ServiceLoader.load(DiagramGenerator.class);
        for (DiagramGenerator generator : generatorLoaderTCCL) {
            generatorMap.put(generator.getName(), generator);
        }
        return generatorMap;
    }

    protected void processRequests(InputStream inputStream, OutputStream outputStream) throws IOException {
        RequestInput input = new HTTPInputStream(inputStream);
        ResponseOutput output = new HTTPOutputStream(outputStream);

        Request request;
        while ((request = input.readRequest()) != null) {
            output.writeResponse(processor.processRequest(request));
            if ("close".equals(request.headers.getValue(HTTPHeader.CONNECTION))) {
                break;
            }
        }
    }
}
