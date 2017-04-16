package org.asciidoctor.diagram;

import java.util.HashMap;
import java.util.Map;

public abstract class HTTPHeader<V> {
    public static final HTTPHeader<MimeType> ACCEPT = new MimeTypeHeader("Accept");
    public static final HTTPHeader<Integer> CONTENT_LENGTH = new IntegerHeader("Content-Length");
    public static final HTTPHeader<MimeType> CONTENT_TYPE = new MimeTypeHeader("Content-Type");
    public static final HTTPHeader<String> HOST = new StringHeader("Host");
    public static final HTTPHeader<String> CONNECTION = new StringHeader("Connection");
    public static final HTTPHeader<String> OPTIONS = new StringHeader("X-Options");

    private static final Map<String, HTTPHeader> HEADERS;
    static {
        HashMap<String, HTTPHeader> headers = new HashMap<String, HTTPHeader>();
        addHeader(headers, ACCEPT);
        addHeader(headers, CONTENT_LENGTH);
        addHeader(headers, CONTENT_TYPE);
        HEADERS = headers;
    }

    private static void addHeader(HashMap<String, HTTPHeader> headers, HTTPHeader header) {
        headers.put(header.name.toLowerCase(), header);
    }

    public static HTTPHeader getHeader(String name) {
        HTTPHeader httpHeader = HEADERS.get(name.toLowerCase());
        if (httpHeader != null) {
            return httpHeader;
        }

        return new StringHeader(name);
    }

    public final String name;
    public final Class<V> type;

    private HTTPHeader(String name, Class<V> type) {
        this.name = name;
        this.type = type;
    }

    public abstract V parseValue(String aValue);

    public abstract String formatValue(V aValue);

    private static class MimeTypeHeader extends HTTPHeader<MimeType> {
        public MimeTypeHeader(String name) {
            super(name, MimeType.class);
        }

        @Override
        public MimeType parseValue(String aValue) {
            return MimeType.parse(aValue);
        }

        @Override
        public String formatValue(MimeType aValue) {
            return aValue.toString();
        }
    }

    private static class StringHeader extends HTTPHeader<String> {
        public StringHeader(String name) {
            super(name, String.class);
        }

        @Override
        public String parseValue(String aValue) {
            return aValue;
        }

        @Override
        public String formatValue(String aValue) {
            return aValue;
        }
    }

    private static class IntegerHeader extends HTTPHeader<Integer> {
        public IntegerHeader(String name) {
            super(name, Integer.class);
        }

        @Override
        public Integer parseValue(String aValue) {
            return Integer.valueOf(aValue);
        }

        @Override
        public String formatValue(Integer aValue) {
            return aValue.toString();
        }
    }
}
