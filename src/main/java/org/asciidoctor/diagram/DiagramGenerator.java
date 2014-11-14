package org.asciidoctor.diagram;

import java.io.IOException;

interface DiagramGenerator {
    ResponseData generate(Request request) throws IOException;
}
