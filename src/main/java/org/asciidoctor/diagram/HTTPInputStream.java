package org.asciidoctor.diagram;

import java.io.*;
import java.net.URI;
import java.util.regex.Pattern;

class HTTPInputStream extends FilterInputStream implements RequestInput, ResponseInput {

    public static final Pattern START_LINE_SPLIT_RE = Pattern.compile(" +");

    public HTTPInputStream(InputStream in) {
        super(new BufferedInputStream(in));
    }

    public String readLine() throws IOException {
        StringBuilder b = new StringBuilder();

        boolean cr = false;
        while (true) {
            int value = in.read();

            if (value == -1) {
                throw new EOFException();
            } else if (value == '\r') {
                if (cr) {
                    b.append('\r');
                }
                cr = true;
            } else if (value == '\n' && cr) {
                break;
            } else {
                if (cr) {
                    b.append('\r');
                    cr = false;
                }
                b.append((char) value);
            }
        }

        return b.toString();
    }

    public void readAll(byte[] data) throws IOException {
        int toRead = data.length;
        int offset = 0;
        while (toRead > 0) {
            int bytesRead = in.read(data, offset, toRead);
            toRead -= bytesRead;
            offset += bytesRead;
        }
    }

    public Request readRequest() throws IOException {
        String line;
        try {
            line = readLine();
            if (line == null) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        if (line.isEmpty()) {
            // Ignore empty line before Status-Line/Request-Line (see RFC 2616 section 4.1)
            line = readLine();
        }


        String requestLine = line;
        String[] parts = START_LINE_SPLIT_RE.split(requestLine, 0);
        //String method = parts[0];
        URI requestUri = URI.create(parts[1]);
        //String httpVersion = parts[2];

        HTTPHeaders headers = readHeaders();
        byte[] data = readData(headers);

        return new Request(requestUri, headers, data);
    }

    @Override
    public Response readResponse() throws IOException {
        String line = readLine();
        if (line.isEmpty()) {
            // Ignore empty line before Status-Line/Request-Line (see RFC 2616 section 4.1)
            line = readLine();
        }


        String statusLine = line;
        String[] parts = START_LINE_SPLIT_RE.split(statusLine, 0);
        int code = Integer.parseInt(parts[1]);
        String reason = parts[2];

        HTTPHeaders headers = readHeaders();
        byte[] data = readData(headers);

        return new Response(
                code,
                headers,
                data
        );
    }

    private byte[] readData(HTTPHeaders headers) throws IOException {
        byte[] data;

        Integer length = headers.getValue(HTTPHeader.CONTENT_LENGTH);
        if (length != null) {
            data = new byte[length.intValue()];
            readAll(data);
        } else {
            data = null;
        }
        return data;
    }

    private HTTPHeaders readHeaders() throws IOException {
        String line;
        HTTPHeaders headers = new HTTPHeaders();
        while (!(line = readLine()).isEmpty()) {
            int i = line.indexOf(':');
            String key = line.substring(0, i);
            String value = line.substring(i + 1).trim();
            headers.putValue(key, value);
        }
        return headers;
    }

}
