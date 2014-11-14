package org.asciidoctor.diagram;

class ResponseData {
    public final MimeType format;
    public final byte[] data;

    public ResponseData(MimeType format, byte[] data) {
        this.format = format;
        this.data = data;
    }
}
