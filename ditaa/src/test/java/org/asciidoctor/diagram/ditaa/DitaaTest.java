package org.asciidoctor.diagram.ditaa;

import org.asciidoctor.diagram.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

public class DitaaTest {
    public static final String DITAA_INPUT =
            """
            +--------+   +-------+    +-------+
            |        | --+ ditaa +--> |       |
            |  Text  |   +-------+    |diagram|
            |Document|   |!magic!|    |       |
            |     {d}|   |       |    |       |
            +---+----+   +-------+    +-------+
                :                         ^
                |       Lots of work      |
                +-------------------------+\
            """;

    @Test
    public void testPNGGeneration() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);
        
        ResponseData response = new Ditaa().generate(new Request(
                URI.create("/ditaa"),
                h,
                DITAA_INPUT.getBytes(Charset.forName("UTF-8"))
        ));

        Assert.assertIsPNG(response);
    }

    @Test(expected = IOException.class)
    public void testBadOutputFormat() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.parse("image/webp"));

        new Ditaa().generate(new Request(
                URI.create("/ditaa"),
                h,
                DITAA_INPUT.getBytes(Charset.forName("UTF-8"))
        ));
    }

    @Test(expected = IOException.class)
    public void testBadInputFormat() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.PNG);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);

        new Ditaa().generate(new Request(
                URI.create("/ditaa"),
                h,
                DITAA_INPUT.getBytes(Charset.forName("UTF-8"))
        ));
    }
}
