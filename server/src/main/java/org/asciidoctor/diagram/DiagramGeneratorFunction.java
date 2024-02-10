package org.asciidoctor.diagram;

import java.io.IOException;

public interface DiagramGeneratorFunction {
    ResponseData generate(Request request) throws IOException;
}
