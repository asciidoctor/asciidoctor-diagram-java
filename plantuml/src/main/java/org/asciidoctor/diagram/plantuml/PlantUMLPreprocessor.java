package org.asciidoctor.diagram.plantuml;

import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.core.UmlSource;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.preproc.Defines;
import net.sourceforge.plantuml.security.SFile;
import org.asciidoctor.diagram.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PlantUMLPreprocessor implements DiagramGenerator
{
    private static final MimeType DEFAULT_OUTPUT_FORMAT = MimeType.TEXT_PLAIN_UTF8;

    @Override
    public String getName()
    {
        return "plantumlpreprocessor";
    }

    @Override
    public ResponseData generate(Request request) throws IOException {
        MimeType format = request.headers.getValue(HTTPHeader.ACCEPT);

        if (format == null) {
            format = DEFAULT_OUTPUT_FORMAT;
        }

        if (!format.equals(MimeType.TEXT_PLAIN_UTF8)) {
            throw new IOException("Unsupported output format: " + format);
        }

        String default_includeDir = System.getProperty("plantuml.include.path");
        String preprocessed;

        try {
            synchronized (this) {
                List<String> includeDirs = request.headers.getValues("X-PlantUML-IncludeDir");
                if (!includeDirs.isEmpty()) {
                    System.setProperty("plantuml.include.path", includeDirs.stream().collect(Collectors.joining(File.pathSeparator)));
                }

                preprocessed = preprocess(
                        request.asString(),
                        request.headers.getValue("X-PlantUML-Config"),
                        request.headers.getValue("X-PlantUML-Basedir")
                );
            }
        } finally {
            if (default_includeDir != null){
                System.setProperty("plantuml.include.path", default_includeDir);
            } else {
                System.clearProperty("plantuml.include.path");
            }
        }

        return new ResponseData(
                format,
                preprocessed.getBytes(StandardCharsets.UTF_8)
        );
    }

    static String preprocess(String input, String plantUmlConfig, String baseDir) throws IOException {
        Option option = new Option();

        if (plantUmlConfig != null) {
            option.initConfig(plantUmlConfig);
        }

        StringBuilder out = new StringBuilder();

        BlockUmlBuilder builder = new BlockUmlBuilder(
                option.getConfig(),
                "UTF-8",
                Defines.createEmpty(),
                new StringReader(input),
                baseDir != null ? new SFile(baseDir) : FileSystem.getInstance().getCurrentDir(),
                "<input>"
        );
        List<BlockUml> blocks = builder.getBlockUmls();

        if (blocks.size() == 0) {
            throw new IOException("No @startuml found");
        } else {
            for (BlockUml b : blocks) {
                Diagram system = b.getDiagram();
                if (system instanceof PSystemError) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    system.exportDiagram(baos, 0, new FileFormatOption(FileFormat.UTXT));
                    throw new IOException(new String(baos.toByteArray(), StandardCharsets.UTF_8));
                }

                UmlSource source = system.getSource();
                if (source != null) {
                    Iterator lines = source.iterator2();
                    while(lines.hasNext()) {
                        Object line = lines.next();
                        if (out.length() > 0) {
                            out.append('\n');
                        }
                        out.append(line);
                    }
                }
            }
        }

        if (out.length() == 0) {
            return "@startuml\n@enduml";
        } else {
            return out.toString();
        }
    }
}
