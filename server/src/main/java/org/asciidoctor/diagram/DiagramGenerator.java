package org.asciidoctor.diagram;

import java.io.IOException;

public interface DiagramGenerator {
    String getName();

    ResponseData generate(Request request) throws IOException;
}
