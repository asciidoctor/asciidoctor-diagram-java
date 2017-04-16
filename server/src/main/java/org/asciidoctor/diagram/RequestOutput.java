package org.asciidoctor.diagram;

import java.io.Closeable;
import java.io.IOException;

interface RequestOutput extends Closeable {
    void writeRequest(Request request) throws IOException;
}
