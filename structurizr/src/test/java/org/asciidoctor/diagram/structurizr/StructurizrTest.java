package org.asciidoctor.diagram.structurizr;

import java.nio.file.Path;
import org.asciidoctor.diagram.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;

class StructurizrTest {
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

    private static final String INPUT_WITH_INCLUDE = """
            workspace {
                !include dsl-include.dsl
                model {
                    user = person "User" "A user of my software system."
                    softwareSystem = softwareSystem "Software System" "My software system."
            
                    user -> softwareSystem "Uses" ${SOAP}
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
    void simple() throws IOException
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

    @Test
    void includesShouldBeResolved() throws IOException
    {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, Structurizr.PLANTUML);
        h.putValue(Structurizr.VIEW_HEADER, "SystemContext");
        // path needs to be a (non-existing) file inside the targeted directory.
        h.putValue("X-Structurizr-IncludeDir", Path.of("src/test/resources/stdin").toAbsolutePath().toString());

        ResponseData responseData = new Structurizr().generate(new Request(
            URI.create("/structurizr"),
            h,
            INPUT_WITH_INCLUDE.getBytes(Charsets.UTF8)
        ));

        assertTrue(responseData.format.isSameType(Structurizr.PLANTUML));

        String text = new String(responseData.data, responseData.format.parameters.getOrDefault("charset", "utf-8"));
        assertTrue(text.startsWith("@startuml"));
        assertTrue(text.contains("SOAP/XML"));
    }

}
