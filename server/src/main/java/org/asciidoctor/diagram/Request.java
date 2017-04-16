package org.asciidoctor.diagram;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

public class Request {
    public final URI requestUri;
    public final byte[] data;
    public final HTTPHeaders headers;

    public Request(URI requestUri, HTTPHeaders headers, byte[] data) {
        this.requestUri = requestUri;
        this.data = data;
        this.headers = headers;
    }

    public String asString() throws IOException {
        if (data == null) {
            throw new IOException("Request has no body");
        }

        MimeType mimeType = headers.getValue(HTTPHeader.CONTENT_TYPE);

        if (mimeType == null) {
            throw new IOException("Cannot convert data when mime type is null");
        }

        if (mimeType.mainType.equalsIgnoreCase("text") && mimeType.subType.equalsIgnoreCase("plain")) {
            String charset = mimeType.parameters.get("charset");
            if (charset != null) {
                return new String(data, charset);
            } else {
              return new String(data);
            }
        } else {
            throw new IOException("Data of type " + mimeType + " cannot be converted to String");
        }
    }
}
