package org.asciidoctor.diagram;

import java.io.Closeable;
import java.io.IOException;

interface RequestInput extends Closeable {
    Request readRequest() throws IOException;
}
