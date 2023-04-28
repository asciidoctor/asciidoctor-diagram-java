package org.asciidoctor.diagram.syntrax;

import org.asciidoctor.diagram.Assert;
import org.asciidoctor.diagram.HTTPHeader;
import org.asciidoctor.diagram.HTTPHeaders;
import org.asciidoctor.diagram.MimeType;
import org.asciidoctor.diagram.Request;
import org.asciidoctor.diagram.ResponseData;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SyntraxTest {

    private static byte[] SYNTRAX_INPUT;

    @BeforeClass
    public static void setUP() {
        try {
            SYNTRAX_INPUT = Files.readAllBytes(Paths.get("src", "test", "resources", "testinput.spec"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
                SYNTRAX_INPUT
        ));

        Assert.assertIsSVG(response);
    }

    @Test
    public void testGeneratePNG() throws IOException {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);

        ResponseData response = new Syntrax().generate(new Request(
                URI.create("/syntrax"),
                h,
                SYNTRAX_INPUT
        ));

        Assert.assertIsPNG(response);
    }

    @Test
    public void testGenerateWithWrongFormat() throws IOException {
        exceptionRule.expect(IOException.class);
        exceptionRule.expectMessage("Unsupported output format");

        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.TEXT_PLAIN_UTF8);

        new Syntrax().generate(new Request(
                URI.create("/syntrax"),
                h,
                SYNTRAX_INPUT
        ));
    }
}