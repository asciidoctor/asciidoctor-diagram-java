package org.asciidoctor.diagram;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MimeType {
    // This is not compliant with RFC 2045; but is good enough for it's current limited usage.
    private static final Pattern MIME_TYPE_RE = Pattern.compile("([\\w-+]+)/([\\w-+]+)");
    private static final Pattern PARAMETER_RE = Pattern.compile("\\s*;\\s*([\\w-+]+)=([\\w-+]+)");

    public static final MimeType PNG = parse("image/png");
    public static final MimeType SVG = parse("image/svg+xml");
    public static final MimeType TEXT_PLAIN_ASCII = parse("text/plain; charset=us-ascii");
    public static final MimeType TEXT_PLAIN_UTF8 = parse("text/plain; charset=utf-8");

    public final String mainType;
    public final String subType;
    public final Map<String, String> parameters;

    public static MimeType parse(String type) {
        return new MimeType(type);
    }

    private MimeType(String type) {
        Matcher typeMatcher = MIME_TYPE_RE.matcher(type);
        if (!typeMatcher.find()) {
            throw new IllegalArgumentException("Invalid mime type: " + type);
        }
        mainType = typeMatcher.group(1).toLowerCase();
        subType = typeMatcher.group(2).toLowerCase();

        String paramString = type.substring(typeMatcher.end(2));

        HashMap<String, String> params = new HashMap<String, String>();

        Matcher paramMatcher = PARAMETER_RE.matcher(paramString);
        while (paramMatcher.find()) {
            params.put(paramMatcher.group(1), paramMatcher.group(2));
        }

        parameters = Collections.unmodifiableMap(params);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MimeType mimeType = (MimeType) o;

        if (!mainType.equals(mimeType.mainType)) return false;
        if (!parameters.equals(mimeType.parameters)) return false;
        if (!subType.equals(mimeType.subType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mainType.hashCode();
        result = 31 * result + subType.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append(mainType);
        b.append('/');
        b.append(subType);

        for (Map.Entry<String, String> param : parameters.entrySet()) {
            b.append(";");
            b.append(param.getKey());
            b.append('=');
            b.append(param.getValue());
        }

        return b.toString();
    }
}
