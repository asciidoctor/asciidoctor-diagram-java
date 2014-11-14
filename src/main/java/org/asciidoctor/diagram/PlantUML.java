package org.asciidoctor.diagram;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.Option;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.preproc.Defines;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

class PlantUML implements DiagramGenerator {
    public static final MimeType DEFAULT_OUTPUT_FORMAT = MimeType.PNG;

    private static File graphviz = null;

    private static File getGraphviz() throws IOException {
        if (graphviz == null) {
            graphviz = Which.which("dot");
        }

        if (graphviz == null) {
            throw new IOException("Could not find GraphViz 'dot' tool");
        }

        return graphviz;
    }

    public PlantUML() {
    }

    @Override
    public ResponseData generate(Request request) throws IOException {
        OptionFlags.getInstance().setDotExecutable(getGraphviz().getAbsolutePath());

        MimeType format = request.headers.getValue(HTTPHeader.ACCEPT);

        if (format == null) {
            format = DEFAULT_OUTPUT_FORMAT;
        }

        FileFormat fileFormat;
        if (format.equals(MimeType.PNG)) {
            fileFormat = FileFormat.PNG;
        } else if (format.equals(MimeType.SVG)) {
            fileFormat = FileFormat.SVG;
        } else if (format.equals(MimeType.TEXT_PLAIN_UTF8)) {
            fileFormat = FileFormat.UTXT;
        } else if (format.equals(MimeType.TEXT_PLAIN_ASCII)) {
            fileFormat = FileFormat.ATXT;
        } else {
            throw new IOException("Unsupported output format: " + format);
        }

        Option option = new Option();

        String plantUmlConfig = request.headers.getValue("X-PlantUML-Config");
        if (plantUmlConfig != null) {
            option.initConfig(plantUmlConfig);
        }
        option.setFileFormat(fileFormat);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        new SourceStringReader(
                new Defines(),
                request.asString(),
                option.getConfig()
        ).generateImage(byteArrayOutputStream, option.getFileFormatOption());

        return new ResponseData(
                format,
                byteArrayOutputStream.toByteArray()
        );

    }
}
