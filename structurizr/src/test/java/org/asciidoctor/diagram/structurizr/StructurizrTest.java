package org.asciidoctor.diagram.structurizr;

import org.asciidoctor.diagram.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StructurizrTest {
    private static final String INPUT = """
            workspace {
            
                model {
                    user = person "User" "A user of my software system."
                    softwareSystem = softwareSystem "Software System" "My software system."
            
                    user -> softwareSystem "Uses"
                }
            
                views {
                    systemContext softwareSystem "SystemContext" {
                        include *
                        autoLayout
                    }
            
                    styles {
                        element "Software System" {
                            background #1168bd
                            color #ffffff
                        }
                        element "Person" {
                            shape person
                            background #08427b
                            color #ffffff
                        }
                    }
                }
               \s
            }\
            """;

    @Test
    public void testSimple() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, Structurizr.PLANTUML);
        h.putValue(Structurizr.VIEW_HEADER, "SystemContext");

        ResponseData responseData = new Structurizr().generate(new Request(
                URI.create("/structurizr"),
                h,
                INPUT.getBytes(Charsets.UTF8)
        ));

        assertTrue(responseData.format.isSameType(Structurizr.PLANTUML));

        String text = new String(responseData.data, responseData.format.parameters.getOrDefault("charset", "utf-8"));
        assertTrue(text.startsWith("@startuml"));
    }
}
