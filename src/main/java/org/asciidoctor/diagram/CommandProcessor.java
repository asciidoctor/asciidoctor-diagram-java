package org.asciidoctor.diagram;

import java.io.*;

public class CommandProcessor {
    public CommandProcessor() {
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
            DiagramGenerator generator;
            if (requestPath.equalsIgnoreCase("/plantuml")) {
                generator = new PlantUML();
            } else if (requestPath.equalsIgnoreCase("/ditaa")) {
                generator = new Ditaa();
            } else {
                return new Response(
                        404,
                        new HTTPHeaders(),
                        null
                );
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
        e.printStackTrace(p);
        p.flush();
        p.close();

        byte[] bytes = s.toString().getBytes(Charsets.UTF8);

        HTTPHeaders headers = new HTTPHeaders();
        headers.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        headers.putValue(HTTPHeader.CONTENT_LENGTH, bytes.length);

        return new Response(code, headers, bytes);
    }
}
