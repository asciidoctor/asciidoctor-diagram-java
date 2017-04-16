package org.asciidoctor.diagram.ditaa;

import org.asciidoctor.diagram.MimeType;
import org.asciidoctor.diagram.ResponseData;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;

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
            XMLEventReader reader = XMLInputFactory.newFactory().createXMLEventReader(new ByteArrayInputStream(response.data));
            XMLEvent event = reader.nextTag();
            assertEquals(new QName("http://www.w3.org/2000/svg", "svg"), event.asStartElement().getName());
            reader.close();
        } catch (XMLStreamException e) {
            fail(e.getMessage());
        }
    }
}
