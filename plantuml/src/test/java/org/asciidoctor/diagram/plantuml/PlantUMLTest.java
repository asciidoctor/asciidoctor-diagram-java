package org.asciidoctor.diagram.plantuml;

import org.asciidoctor.diagram.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

public class PlantUMLTest {
    private static final String PLANTUML_INPUT = "@startuml\nA -> B\n@enduml";

    @Test
    public void testPNGGeneration() throws IOException
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
    public void testSVGGeneration() throws IOException
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
    public void testMathPNGGeneration() throws IOException
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
    public void testMathSVGGeneration() throws IOException
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

    @Test(expected = IOException.class)
    public void testBadOutputFormat() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.parse("image/webp"));

        new PlantUML().generate(new Request(
                URI.create("/plantuml"),
                h,
                PLANTUML_INPUT.getBytes(Charsets.UTF8)
        ));
    }

    @Test(expected = IOException.class)
    public void testBadInputFormat() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.PNG);
        h.putValue(HTTPHeader.ACCEPT, MimeType.SVG);

        new PlantUML().generate(new Request(
                URI.create("/plantuml"),
                h,
                PLANTUML_INPUT.getBytes(Charsets.UTF8)
        ));
    }

    @Test(expected = IOException.class)
    public void testSyntaxErrors() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.SVG);

        new PlantUML().generate(new Request(
                URI.create("/plantuml"),
                h,
                "@startuml\nBob; sdf; foo\n@enduml".getBytes(Charsets.UTF8)
        ));
    }
}
