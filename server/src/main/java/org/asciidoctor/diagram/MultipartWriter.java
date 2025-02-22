package org.asciidoctor.diagram;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MultipartWriter {
    private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.US_ASCII);

    private final ByteArrayOutputStream outputStream;
    private final MimeType format;
    private final byte[] startBoundary;
    private final byte[] endBoundary;

    public MultipartWriter() {
        outputStream = new ByteArrayOutputStream();
        String boundary = UUID.randomUUID().toString();
        format = MimeType.MULTIPART_FORM_DATA.withParameter("boundary", boundary);
        startBoundary = ("--" + boundary).getBytes(StandardCharsets.US_ASCII);
        endBoundary = ("--" + boundary + "--").getBytes(StandardCharsets.US_ASCII);
    }

    public MimeType getFormat() {
        return format;
    }

    public void addPart(String name, MimeType contentType, byte[] content) throws IOException {
        outputStream.write(CRLF);
        outputStream.write(startBoundary);
        outputStream.write(CRLF);

        outputStream.write("Content-Disposition: form-data; name=\"".getBytes(StandardCharsets.US_ASCII));
        // TODO escape "
        outputStream.write(name.getBytes(StandardCharsets.US_ASCII));
        outputStream.write("\"".getBytes(StandardCharsets.US_ASCII));
        outputStream.write(CRLF);

        outputStream.write("Content-Type: ".getBytes(StandardCharsets.US_ASCII));
        outputStream.write(contentType.toString().getBytes(StandardCharsets.US_ASCII));
        outputStream.write(CRLF);

        // Strictly speaking Content-Length is not allowed in multipart/form-data
        // Having it in place makes parsing on the receiving side much simpler though,
        // and I'm not too concerned about interoperability at the moment.
        outputStream.write("Content-Length: ".getBytes(StandardCharsets.US_ASCII));
        outputStream.write(Integer.toString(content.length).getBytes(StandardCharsets.US_ASCII));
        outputStream.write(CRLF);

        outputStream.write(CRLF);
        outputStream.write(content);
    }

    public byte[] finish() throws IOException {
        outputStream.write(CRLF);
        outputStream.write(endBoundary);
        outputStream.write(CRLF);

        return outputStream.toByteArray();
    }
}
