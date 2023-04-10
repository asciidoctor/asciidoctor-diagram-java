package org.asciidoctor.diagram.syntrax;

import org.asciidoctor.diagram.*;
import org.atpfivt.jsyntrax.InputArguments;
import org.atpfivt.jsyntrax.styles.StyleConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.atpfivt.jsyntrax.Main.generateSVG;
import static org.atpfivt.jsyntrax.Main.getStyleConfig;

public class Syntrax implements DiagramGenerator {
    public static final MimeType DEFAULT_OUTPUT_FORMAT = MimeType.SVG;

    @Override
    public String getName() {
        return "syntrax";
    }

    @Override
    public ResponseData generate(Request request) throws IOException {
        MimeType format = request.headers.getValue(HTTPHeader.ACCEPT);
        if (format == null) {
            format = DEFAULT_OUTPUT_FORMAT;
        }

        if (!format.equals(MimeType.SVG)) {
            throw new IOException("Unsupported output format: " + format);
        }

        String[] options = new String[0];
        String optionString = request.headers.getValue(HTTPHeader.OPTIONS);
        if (optionString != null) {
            options = optionString.split(" ");
        }

        InputArguments iArgs;
        try {
            iArgs = new InputArguments(options);
        } catch (Exception e) {
            throw new IOException("Got exception when parsing input arguments: " + e.getMessage());
        }

        StyleConfig style = getStyleConfig(iArgs);

        String title = iArgs.getTitle();
        String responseData = generateSVG(title, style, new String(request.data, StandardCharsets.UTF_8));

        return new ResponseData(
                format,
                responseData.getBytes(StandardCharsets.UTF_8)
        );
    }
}
