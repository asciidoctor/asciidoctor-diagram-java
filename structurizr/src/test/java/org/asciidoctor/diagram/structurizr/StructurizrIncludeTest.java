package org.asciidoctor.diagram.structurizr;

import org.asciidoctor.diagram.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class StructurizrIncludeTest {
    private static final String INCLUDE_FILE_NAME = "dsl-include.dsl";

    private static final String INPUT_WITH_INCLUDE = "workspace {\n" +
            "     !include " + INCLUDE_FILE_NAME + "\n" +
            "     model {\n" +
            "        user = person \"User\" \"A user of my software system.\"\n" +
            "        softwareSystem = softwareSystem \"Software System\" \"My software system.\"\n" +
            "\n" +
            "        user -> softwareSystem \"Uses\" ${SOAP}\n" +
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
            "}";

    private Path tempDir;

    @Before
    public void createTempDir() throws IOException {
        tempDir = Files.createTempDirectory("structurizr");
    }

    @After
    public void deleteTempDir() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Test
    public void includesShouldBeResolved() throws IOException
    {
        Files.write(
                tempDir.resolve(INCLUDE_FILE_NAME),
                List.of("!constant \"SOAP\" \"SOAP/XML\""),
                StandardCharsets.UTF_8
        );

        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        h.putValue(HTTPHeader.ACCEPT, Structurizr.PLANTUML);
        h.putValue(Structurizr.VIEW_HEADER, "SystemContext");
        // path needs to be a (non-existing) file inside the targeted directory.
        h.putValue("X-Structurizr-IncludeDir", tempDir.toAbsolutePath().toString());

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
