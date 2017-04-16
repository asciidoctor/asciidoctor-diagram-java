package org.asciidoctor.diagram;

public class Response {
    public final int code;
    public final String reason;
    public final HTTPHeaders headers;
    public final byte[] data;

    public Response(int code, HTTPHeaders headers, byte[] data) {
        this.code = code;
        this.reason = getReason(code);
        this.headers = headers;
        this.data = data;
    }

    private static String getReason(int code) {
        switch (code) {
            case 200:
                return "OK";
            case 400:
                return "Bad Request";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown";
        }
    }
}
