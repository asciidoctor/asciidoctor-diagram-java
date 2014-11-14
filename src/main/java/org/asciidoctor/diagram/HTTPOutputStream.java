package org.asciidoctor.diagram;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

class HTTPOutputStream extends FilterOutputStream implements ResponseOutput, RequestOutput {
    private static final byte[] HTML_VERSION = "HTTP/1.1".getBytes(Charsets.US_ASCII);

    public HTTPOutputStream(OutputStream out) {
        super(new BufferedOutputStream(out));
    }

    public void write(String string) throws IOException {
        out.write(string.getBytes(Charsets.US_ASCII));
    }

    public void writeStatusLine(int code, String reason) throws IOException {
        out.write(HTML_VERSION);
        out.write(' ');
        write(Integer.toString(code));
        out.write(' ');
        write(reason);
        newline();
    }

    public void writeRequestLine(String method, URI requestURI) throws IOException {
        write(method);
        out.write(' ');
        write(requestURI.toString());
        out.write(' ');
        out.write(HTML_VERSION);
        newline();
    }

    public void writeHeader(String key, String value) throws IOException {
        write(key);
        write(": ");
        write(value);
        newline();
    }

    public void newline() throws IOException {
        out.write('\r');
        out.write('\n');
    }

    public void writeResponse(Response response) throws IOException {
        writeStatusLine(response.code, response.reason);
        writeHeaders(response.headers);
        if (response.headers.getValue(HTTPHeader.CONTENT_LENGTH.name) == null && response.data != null) {
            writeHeader(HTTPHeader.CONTENT_LENGTH.name, Integer.toString(response.data.length));
        }
        newline();

        if (response.data != null) {
            out.write(response.data);
        }
        out.flush();
    }

    public void writeRequest(Request request) throws IOException {
        writeRequestLine("GET", request.requestUri);
        writeHeaders(request.headers);
        if (request.headers.getValue(HTTPHeader.CONTENT_LENGTH.name) == null && request.data != null) {
            writeHeader(HTTPHeader.CONTENT_LENGTH.name, Integer.toString(request.data.length));
        }
        newline();

        if (request.data != null) {
            out.write(request.data);
        }
        out.flush();
    }

    private void writeHeaders(HTTPHeaders headers) throws IOException {
        for (String key : headers.keys()) {
            writeHeader(key, headers.getValue(key));
        }
    }
}
