package org.asciidoctor.diagram;

import java.util.*;
import java.util.stream.Collectors;

public class HTTPHeaders {
    private Map<String, List<String>> headers;

    public HTTPHeaders() {
        this.headers = new HashMap<>();
    }

    public void putValue(String name, String value) {
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    public <V> void putValue(HTTPHeader<V> h, V value) {
        putValue(h.name, h.formatValue(value));
    }

    public String getValue(String name) {
        List<String> values = getValues(name);
        return !values.isEmpty() ? values.get(0) : null;
    }

    public <V> V getValue(HTTPHeader<V> h) {
        List<V> values = getValues(h);
        return !values.isEmpty() ? values.get(0) : null;
    }

    public List<String> getValues(String name) {
        List<String> values = headers.get(name);
        return values == null ? Collections.emptyList() : values;
    }

    public <V> List<V> getValues(HTTPHeader<V> h) {
        return getValues(h.name).stream().map(h::parseValue).collect(Collectors.toList());
    }

    public Iterable<String> keys() {
        return headers.keySet();
    }
}
