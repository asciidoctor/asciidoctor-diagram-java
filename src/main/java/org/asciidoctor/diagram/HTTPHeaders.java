package org.asciidoctor.diagram;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class HTTPHeaders {
    private Map<String, String> headers;

    public HTTPHeaders() {
        this.headers = new HashMap<String, String>();
    }

    public void putValue(String name, String value) {
        headers.put(name, value);
    }

    public String getValue(String name) {
        return headers.get(name);
    }

    public <V> void putValue(HTTPHeader<V> h, V value) {
        putValue(h.name, h.formatValue(value));
    }

    public <V> V getValue(HTTPHeader<V> h) {
        String value = getValue(h.name);
        if (value == null) {
            return null;
        } else {
            return h.parseValue(value);
        }
    }

    public Iterable<String> keys() {
        return headers.keySet();
    }
}
