package org.asciidoctor.diagram.syntrax;

import org.asciidoctor.diagram.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class SyntraxTest {

    public static final String SYNTRAX_INPUT = "jsyntrax(stack(\n" +
            " line('attribute', '/(attribute) identifier', 'of'),\n" +
            " line(choice(toploop('/entity_designator', ','), 'others', 'all'), ':'),\n" +
            " line('/entity_class', 'is', '/expression', ';')\n" +
            "), \n" +
            "[\n" +
            "  'entity_class': 'https://www.google.com/#q=vhdl+entity+class',\n" +
            "  '(attribute) identifier': 'http://en.wikipedia.com/wiki/VHDL'\n" +
            "])";
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testTestGetName() {
        org.junit.Assert.assertEquals("syntrax", new Syntrax().getName());
    }

    @Test
    public void testGenerateSVG() throws IOException {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.SVG);

        ResponseData response = new Syntrax().generate(new Request(
                URI.create("/syntrax"),
                h,
                SYNTRAX_INPUT.getBytes(StandardCharsets.UTF_8)
        ));

        Assert.assertIsSVG(response);
    }

    @Test
    public void testGenerateWithWrongFormat() throws IOException {
        exceptionRule.expect(IOException.class);
        exceptionRule.expectMessage("Unsupported output format");

        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);

        new Syntrax().generate(new Request(
                URI.create("/ditaa"),
                h,
                SYNTRAX_INPUT.getBytes(StandardCharsets.UTF_8)
        ));
    }
}