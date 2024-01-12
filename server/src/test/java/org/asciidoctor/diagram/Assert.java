package org.asciidoctor.diagram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Assert
{
    public static void assertIsPNG(ResponseData response) {
        assertEquals(MimeType.PNG, response.format);

        byte[] data = response.data;
        assertEquals(0x89, data[0] & 0xFF);
        assertEquals(0x50, data[1] & 0xFF);
        assertEquals(0x4E, data[2] & 0xFF);
        assertEquals(0x47, data[3] & 0xFF);
    }

    public static void assertIsSVG(ResponseData response) {
        assertEquals(MimeType.SVG, response.format);

        Assertions.assertDoesNotThrow(() -> {
            Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(response.data));
            Element rootElement = dom.getDocumentElement();
            String tag = rootElement.getTagName();
            assertEquals("svg", tag);
        });
    }
}
