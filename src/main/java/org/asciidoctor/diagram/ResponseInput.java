package org.asciidoctor.diagram;

import java.io.Closeable;
import java.io.IOException;

interface ResponseInput extends Closeable {
    Response readResponse() throws IOException;
}
