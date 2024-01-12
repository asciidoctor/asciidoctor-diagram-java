package org.asciidoctor.diagram.plantuml;

import org.asciidoctor.diagram.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PlantUMLPreprocessorTest {

    @Test
    void simple() throws IOException
    {
        String output = preprocess("@startuml\nA -> B\n@enduml");
        Assertions.assertEquals(
                "@startuml\nA -> B\n@enduml",
                output
        );
    }

    @Test
    void simpleInclude() throws IOException
    {
        String output = preprocess("@startuml\n!include " + getAbsolutePath("file2.puml") + "\n@enduml");
        Assertions.assertEquals(
                "@startuml\nclass B\n@enduml",
                output
        );
    }

    @Test
    void transitiveInclude() throws IOException
    {
        String output = preprocess("@startuml\n!include " + getAbsolutePath("file1.puml") + "\n@enduml");
        Assertions.assertEquals(
                "@startuml\nclass A\nclass B\n@enduml",
                output
        );
    }

    @Test
    void includeDir() throws IOException
    {
        System.setProperty("plantuml.include.path", Paths.get("doesnotexist").toAbsolutePath().toString());

        String output = preprocess(
                "@startuml\n!include common.puml\nclass B\n@enduml",
                Paths.get("someotherdir").toAbsolutePath().toString(),
                getAbsolutePath("include/")
        );
        Assertions.assertEquals(
                "@startuml\nclass C\nclass B\n@enduml",
                output
        );
    }

    @Test
    void includeErrors() throws IOException
    {
        assertThrows(IOException.class, () -> {
            String output = preprocess("@startuml\n!include " + getAbsolutePath("error.puml") + "\n@enduml");
            Assertions.assertEquals(
                    "@startuml\nclass A\nclass B\n@enduml",
                    output
            );
        });
    }

    private String getAbsolutePath(String fileName) throws IOException {
        try {
            return new File(getClass().getClassLoader().getResource(fileName).toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private String preprocess(String input) throws IOException {
        return preprocess(input, Collections.emptyList());
    }

    private String preprocess(String input, String... includeDirs) throws IOException {
        return preprocess(input, Arrays.asList(includeDirs));
    }

    private String preprocess(String input, List<String> includeDirs) throws IOException {
        HTTPHeaders h = new HTTPHeaders();
        h.putValue(HTTPHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN_UTF8);
        for (String includeDir : includeDirs) {
            h.putValue("X-PlantUML-IncludeDir", includeDir);
        }

        ResponseData responseData = new PlantUMLPreprocessor().generate(new Request(
                URI.create("/plantumlpreprocessor"),
                h,
                input.getBytes(Charsets.UTF8)
        ));

        return new String(responseData.data, StandardCharsets.UTF_8);
    }
}
