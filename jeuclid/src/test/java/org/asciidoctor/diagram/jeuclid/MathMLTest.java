package org.asciidoctor.diagram.jeuclid;

import org.asciidoctor.diagram.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class MathMLTest {
    @Test
    public void testMathML() throws IOException {
        HTTPHeaders headers = new HTTPHeaders();
        headers.putValue(HTTPHeader.ACCEPT, MimeType.SVG);
        headers.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);

        ResponseData responseData = new MathML().generate(new Request(
                URI.create("http://127.0.0.1/jeuclid"),
                headers,
                ("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" display=\"block\">\n" +
                        "  <mi>x</mi>\n" +
                        "  <mo>=</mo>\n" +
                        "  <mrow data-mjx-texclass=\"ORD\">\n" +
                        "    <mfrac>\n" +
                        "      <mrow>\n" +
                        "        <mo>&#x2212;</mo>\n" +
                        "        <mi>b</mi>\n" +
                        "        <mo>&#xB1;</mo>\n" +
                        "        <msqrt>\n" +
                        "          <msup>\n" +
                        "            <mi>b</mi>\n" +
                        "            <mn>2</mn>\n" +
                        "          </msup>\n" +
                        "          <mo>&#x2212;</mo>\n" +
                        "          <mn>4</mn>\n" +
                        "          <mi>a</mi>\n" +
                        "          <mi>c</mi>\n" +
                        "        </msqrt>\n" +
                        "      </mrow>\n" +
                        "      <mrow>\n" +
                        "        <mn>2</mn>\n" +
                        "        <mi>a</mi>\n" +
                        "      </mrow>\n" +
                        "    </mfrac>\n" +
                        "  </mrow>\n" +
                        "  <mo>.</mo>\n" +
                        "</math>").getBytes(StandardCharsets.UTF_8)
        ));

        String svg = new String(responseData.data, StandardCharsets.UTF_8);
        Assert.assertTrue(svg.contains("<svg"));
    }
}
