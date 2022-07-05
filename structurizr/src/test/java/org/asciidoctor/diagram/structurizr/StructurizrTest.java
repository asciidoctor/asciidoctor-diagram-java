package org.asciidoctor.diagram.structurizr;

import org.asciidoctor.diagram.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StructurizrTest {
    private static final String INPUT = "workspace {\n" +
            "\n" +
            "    model {\n" +
            "        user = person \"User\" \"A user of my software system.\"\n" +
            "        softwareSystem = softwareSystem \"Software System\" \"My software system.\"\n" +
            "\n" +
            "        user -> softwareSystem \"Uses\"\n" +
            "    }\n" +
            "\n" +
            "    views {\n" +
            "        systemContext softwareSystem \"SystemContext\" {\n" +
            "            include *\n" +
            "            autoLayout\n" +
            "        }\n" +
            "\n" +
            "        styles {\n" +
            "            element \"Software System\" {\n" +
            "                background #1168bd\n" +
            "                color #ffffff\n" +
            "            }\n" +
            "            element \"Person\" {\n" +
            "                shape person\n" +
            "                background #08427b\n" +
            "                color #ffffff\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "}";

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
