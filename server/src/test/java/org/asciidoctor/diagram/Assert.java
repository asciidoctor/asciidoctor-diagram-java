package org.asciidoctor.diagram;

import org.asciidoctor.diagram.MimeType;
import org.asciidoctor.diagram.ResponseData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

        try {
            Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(response.data));
            Element rootElement = dom.getDocumentElement();
            String tag = rootElement.getTagName();
            assertEquals("svg", tag);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            fail(e.getMessage());
        }
    }
}
