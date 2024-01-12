package org.asciidoctor.diagram.plantuml;

import org.asciidoctor.diagram.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PlantUMLTest {
    private static final String PLANTUML_INPUT = "@startuml\nA -> B\n@enduml";

    @Test
    void pNGGeneration() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);

        ResponseData responseData = new PlantUML().generate(new Request(
                URI.create("/plantuml"),
                h,
                PLANTUML_INPUT.getBytes(Charsets.UTF8)
        ));

        Assert.assertIsPNG(responseData);
    }

    @Test
    void sVGGeneration() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.SVG);

        ResponseData responseData = new PlantUML().generate(new Request(
                URI.create("/plantuml"),
                h,
                PLANTUML_INPUT.getBytes(Charsets.UTF8)
        ));

        Assert.assertIsSVG(responseData);
    }

    private static final String PLANTUML_MATH_INPUT = "@startlatex\n\\sum_{i=0}^{n-1} (a_i + b_i^2)\n@endlatex";

    @Test
    void mathPNGGeneration() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);

        ResponseData responseData = new PlantUML().generate(new Request(
                URI.create("/plantuml"),
                h,
                PLANTUML_MATH_INPUT.getBytes(Charsets.UTF8)
        ));

        Assert.assertIsPNG(responseData);
    }

    @Test
    void mathSVGGeneration() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.SVG);

        ResponseData responseData = new PlantUML().generate(new Request(
                URI.create("/plantuml"),
                h,
                PLANTUML_MATH_INPUT.getBytes(Charsets.UTF8)
        ));

        Assert.assertIsSVG(responseData);
    }

    @Test
    void badOutputFormat() throws IOException
    {
        assertThrows(IOException.class, () -> {
            HTTPHeaders h = new HTTPHeaders();
            h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
            h.putValue(HTTPHeader.ACCEPT, MimeType.parse("image/webp"));

            new PlantUML().generate(new Request(
                    URI.create("/plantuml"),
                    h,
                    PLANTUML_INPUT.getBytes(Charsets.UTF8)
            ));
        });
    }

    @Test
    void badInputFormat() throws IOException
    {
        assertThrows(IOException.class, () -> {
            HTTPHeaders h = new HTTPHeaders();
            h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.PNG);
            h.putValue(HTTPHeader.ACCEPT, MimeType.SVG);

            new PlantUML().generate(new Request(
                    URI.create("/plantuml"),
                    h,
                    PLANTUML_INPUT.getBytes(Charsets.UTF8)
            ));
        });
    }

    @Test
    void syntaxErrors() throws IOException
    {
        assertThrows(IOException.class, () -> {
            HTTPHeaders h = new HTTPHeaders();
            h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
            h.putValue(HTTPHeader.ACCEPT, MimeType.SVG);

            new PlantUML().generate(new Request(
                    URI.create("/plantuml"),
                    h,
                    "@startuml\nBob; sdf; foo\n@enduml".getBytes(Charsets.UTF8)
            ));
        });
    }
}
