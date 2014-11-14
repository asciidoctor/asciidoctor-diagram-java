package org.asciidoctor.diagram;

import org.junit.Test;

import java.net.URI;
import java.nio.charset.Charset;

import static org.asciidoctor.diagram.Assert.assertIsPNG;
import static org.asciidoctor.diagram.Assert.assertIsSVG;
import static org.junit.Assert.assertEquals;

public class PlantUMLTest {
    private static final String PLANTUML_INPUT = "@startuml\nA -> B\n@enduml";

    @Test
    public void testPNGGeneration() {
        CommandProcessor server = new CommandProcessor();

        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);

        Response response = server.processRequest(new Request(
                URI.create("/plantuml"),
                h,
                PLANTUML_INPUT.getBytes(Charset.forName("UTF-8"))
        ));

        assertEquals(200, response.code);
        assertIsPNG(response);
    }

    @Test
    public void testSVGGeneration() {
        CommandProcessor server = new CommandProcessor();

        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.SVG);

        Response response = server.processRequest(new Request(
                URI.create("/plantuml"),
                h,
                PLANTUML_INPUT.getBytes(Charset.forName("UTF-8"))
        ));

        assertEquals(200, response.code);
        assertIsSVG(response);
    }

    @Test
    public void testBadOutputFormat() {
        CommandProcessor server = new CommandProcessor();

        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.parse("image/webp"));

        Response response = server.processRequest(new Request(
                URI.create("/plantuml"),
                h,
                PLANTUML_INPUT.getBytes(Charset.forName("UTF-8"))
        ));

        assertEquals(400, response.code);
    }

    @Test
    public void testBadInputFormat() {
        CommandProcessor server = new CommandProcessor();

        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.PNG);
        h.putValue(HTTPHeader.ACCEPT, MimeType.SVG);

        Response response = server.processRequest(new Request(
                URI.create("/plantuml"),
                h,
                PLANTUML_INPUT.getBytes(Charset.forName("UTF-8"))
        ));

        assertEquals(400, response.code);
    }
}
