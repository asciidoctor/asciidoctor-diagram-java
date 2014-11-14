package org.asciidoctor.diagram;

import org.junit.Test;

import java.net.URI;
import java.nio.charset.Charset;

import static org.asciidoctor.diagram.Assert.assertIsPNG;
import static org.junit.Assert.assertEquals;

public class DitaaTest {
    public static final String DITAA_INPUT =
            "+--------+   +-------+    +-------+\n" +
            "|        | --+ ditaa +--> |       |\n" +
            "|  Text  |   +-------+    |diagram|\n" +
            "|Document|   |!magic!|    |       |\n" +
            "|     {d}|   |       |    |       |\n" +
            "+---+----+   +-------+    +-------+\n" +
            "    :                         ^\n" +
            "    |       Lots of work      |\n" +
            "    +-------------------------+";

    @Test
    public void testPNGGeneration() {
        CommandProcessor processor = new CommandProcessor();
        
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);
        
        Response response = processor.processRequest(new Request(
                URI.create("/ditaa"),
                h,
                DITAA_INPUT.getBytes(Charset.forName("UTF-8"))
        ));

        assertEquals(200, response.code);
        assertIsPNG(response);
    }

    @Test
    public void testBadOutputFormat() {
        CommandProcessor processor = new CommandProcessor();

        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, MimeType.parse("image/webp"));
        
        Response response = processor.processRequest(new Request(
                URI.create("/ditaa"),
                h,
                DITAA_INPUT.getBytes(Charset.forName("UTF-8"))
        ));

        assertEquals(400, response.code);
    }

    @Test
    public void testBadInputFormat() {
        CommandProcessor processor = new CommandProcessor();
        
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.PNG);
        h.putValue(HTTPHeader.ACCEPT, MimeType.PNG);

        Response response = processor.processRequest(new Request(
                URI.create("/ditaa"),
                h,
                DITAA_INPUT.getBytes(Charset.forName("UTF-8"))
        ));

        assertEquals(400, response.code);
    }
}
