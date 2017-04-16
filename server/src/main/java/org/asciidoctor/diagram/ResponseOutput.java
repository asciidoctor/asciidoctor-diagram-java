package org.asciidoctor.diagram;

import java.io.Closeable;
import java.io.IOException;

interface ResponseOutput extends Closeable {
    void writeResponse(Response r) throws IOException;
}
