package org.asciidoctor.diagram.syntrax;

import org.asciidoctor.diagram.Assert;
import org.asciidoctor.diagram.HTTPHeader;
import org.asciidoctor.diagram.HTTPHeaders;
import org.asciidoctor.diagram.MimeType;
import org.asciidoctor.diagram.Request;
import org.asciidoctor.diagram.ResponseData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyntraxTest {

    private static byte[] SYNTRAX_INPUT;

    @BeforeAll
    static void setUP() {
        try {
            SYNTRAX_INPUT = Files.readAllBytes(Paths.get("src", "test", "resources", "testinput.spec"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetName() {
        Assertions.assertEquals("syntrax", new Syntrax().getName());
    }

    @Test
    void generateSVG() throws IOException {
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
    void generatePNG() throws IOException {
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
    void generateWithWrongFormat() throws IOException {
        Throwable exception = assertThrows(IOException.class, () -> {

            HTTPHeaders h = new HTTPHeaders();
            h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
            h.putValue(HTTPHeader.ACCEPT, MimeType.TEXT_PLAIN_UTF8);

            new Syntrax().generate(new Request(
                    URI.create("/syntrax"),
                    h,
                    SYNTRAX_INPUT
            ));
        });
        assertTrue(exception.getMessage().contains("Unsupported output format"));
    }
}