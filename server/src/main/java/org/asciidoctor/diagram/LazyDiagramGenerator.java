package org.asciidoctor.diagram;

import java.io.IOException;

public abstract class LazyDiagramGenerator implements DiagramGenerator {
    private final String name;
    private DiagramGeneratorFunction diagramGeneratorFunction;
    private IOException loadError;

    public LazyDiagramGenerator(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected abstract DiagramGeneratorFunction createGenerator() throws Exception;

    public ResponseData generate(Request request) throws IOException {
        if (loadError != null) {
            throw loadError;
        }

        if (diagramGeneratorFunction == null) {
            try {
                diagramGeneratorFunction = createGenerator();
            } catch (Throwable e) {
                String message = getName() + " could not be initialised due to " + e.getClass().getSimpleName();
                if (e.getMessage() != null) {
                    message += ": " + e.getMessage();
                }
                loadError = new IOException(message);
                loadError.setStackTrace(e.getStackTrace());
                throw loadError;
            }
        }

        return diagramGeneratorFunction.generate(request);
    }
}
