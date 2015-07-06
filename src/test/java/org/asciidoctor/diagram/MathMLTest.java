package org.asciidoctor.diagram;

import org.junit.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import static org.asciidoctor.diagram.Assert.assertIsPNG;
import static org.asciidoctor.diagram.Assert.assertIsSVG;
import static org.junit.Assert.assertEquals;

public class MathMLTest {
    private static final String MATHML_INPUT = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n" +
            "  <mstyle displaystyle=\"true\">\n" +
            "    <mrow>\n" +
            "      <munderover>\n" +
            "        <mo>&#x2211;</mo>\n" +
            "        <mrow>\n" +
            "          <mi>i</mi>\n" +
            "          <mo>=</mo>\n" +
            "          <mn>1</mn>\n" +
            "        </mrow>\n" +
            "        <mi>n</mi>\n" +
            "      </munderover>\n" +
            "    </mrow>\n" +
            "    <msup>\n" +
            "      <mi>i</mi>\n" +
            "      <mn>3</mn>\n" +
            "    </msup>\n" +
            "    <mo>=</mo>\n" +
            "    <msup>\n" +
            "      <mrow>\n" +
            "        <mo>(</mo>\n" +
            "        <mfrac>\n" +
            "          <mrow>\n" +
            "            <mi>n</mi>\n" +
            "            <mrow>\n" +
            "              <mo>(</mo>\n" +
            "              <mi>n</mi>\n" +
            "              <mo>+</mo>\n" +
            "              <mn>1</mn>\n" +
            "              <mo>)</mo>\n" +
            "            </mrow>\n" +
            "          </mrow>\n" +
            "          <mn>2</mn>\n" +
            "        </mfrac>\n" +
            "        <mo>)</mo>\n" +
            "      </mrow>\n" +
            "      <mn>2</mn>\n" +
            "    </msup>\n" +
            "  </mstyle>\n" +
            "</math>";

    @Test
    public void testPNGGeneration() throws IOException
    {
        CommandProcessor server = new CommandProcessor();

        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);

        Response response = server.processRequest(new Request(
                URI.create("/mathml"),
                h,
                MATHML_INPUT.getBytes(Charset.forName("UTF-8"))
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
                URI.create("/mathml"),
                h,
                MATHML_INPUT.getBytes(Charset.forName("UTF-8"))
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
                URI.create("/mathml"),
                h,
                MATHML_INPUT.getBytes(Charset.forName("UTF-8"))
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
                URI.create("/mathml"),
                h,
                MATHML_INPUT.getBytes(Charset.forName("UTF-8"))
        ));

        assertEquals(400, response.code);
    }
}
