package org.asciidoctor.diagram;

import java.io.*;
import java.util.Map;

public class CommandProcessor {
    private final Map<String, DiagramGenerator> generatorMap;

    CommandProcessor(Map<String, DiagramGenerator> generators) {
        this.generatorMap = generators;
    }

    public byte[] processRequest(byte[] request) throws IOException {
        Request req = new HTTPInputStream(new ByteArrayInputStream(request)).readRequest();
        Response response = processRequest(req);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new HTTPOutputStream(out).writeResponse(response);
        out.close();
        return out.toByteArray();
    }

    public Response processRequest(Request request) {
        try {
            String requestPath = request.requestUri.getPath();

            String generatorName = requestPath.toLowerCase();
            if (generatorName.startsWith("/") && generatorName.length() > 1) {
                generatorName = generatorName.substring(1);
            }

            DiagramGenerator generator = generatorMap.get(generatorName);
            if (generator == null) {
                return createErrorResponse(404, new IllegalArgumentException("Invalid path '" + requestPath +"'"));
            }

            ResponseData result = generator.generate(request);

            HTTPHeaders headers = new HTTPHeaders();
            headers.putValue(HTTPHeader.CONTENT_LENGTH, result.data.length);
            headers.putValue(HTTPHeader.CONTENT_TYPE, result.format);

            return new Response(
                    200,
                    headers,
                    result.data
            );
        } catch (IOException e) {
            return createErrorResponse(400, e);
        } catch (RuntimeException e) {
            return createErrorResponse(500, e);
        }
    }

    private Response createErrorResponse(int code, Exception e) {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        p.write("{\"msg\":");
        writeJSONString(p, e.getMessage());
        p.write(",\"stk\":[");
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement elem = stackTrace[i];
            if (i > 0) {
                p.write(",");
            }
            p.write('[');
            writeJSONString(p, elem.getFileName());
            p.write(',');
            writeJSONString(p, elem.getClassName());
            p.write(',');
            writeJSONString(p, elem.getMethodName());
            p.write(',');
            p.write(Integer.toString(elem.getLineNumber()));
            p.write(']');
        }
        p.write("]}");
        p.flush();
        p.close();

        byte[] bytes = s.toString().getBytes(Charsets.UTF8);

        HTTPHeaders headers = new HTTPHeaders();
        headers.putValue(HTTPHeader.CONTENT_TYPE, MimeType.JSON_UTF8);
        headers.putValue(HTTPHeader.CONTENT_LENGTH, bytes.length);

        return new Response(code, headers, bytes);
    }

    private void writeJSONString(PrintWriter p, String message)
    {
        if (message == null) {
            p.write("null");
            return;
        }

        p.write('"');
        for (int i = 0; i < message.length(); i++) {
            int c = message.charAt(i);
            switch (c) {
                case '"':
                    p.print("\\\"");
                    break;
                case '\\':
                    p.print("\\\\");
                    break;
                case '\b':
                    p.print("\\b");
                    break;
                case '\f':
                    p.print("\\f");
                    break;
                case '\n':
                    p.print("\\n");
                    break;
                case '\r':
                    p.print("\\r");
                    break;
                case '\t':
                    p.print("\\t");
                    break;
                default:
                    p.write(c);
            }
        }
        p.write('"');
    }
}
