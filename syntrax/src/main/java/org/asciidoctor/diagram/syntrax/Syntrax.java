package org.asciidoctor.diagram.syntrax;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.cli.ParseException;
import org.asciidoctor.diagram.*;
import org.atpfivt.jsyntrax.InputArguments;
import org.atpfivt.jsyntrax.styles.StyleConfig;
import org.atpfivt.jsyntrax.util.SVGTranscoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.atpfivt.jsyntrax.Main.generateSVG;
import static org.atpfivt.jsyntrax.Main.getStyleConfig;

public class Syntrax implements DiagramGeneratorFunction {
    public static final MimeType DEFAULT_OUTPUT_FORMAT = MimeType.SVG;

    @Override
    public ResponseData generate(Request request) throws IOException {
        MimeType format = request.headers.getValue(HTTPHeader.ACCEPT);
        if (format == null) {
            format = DEFAULT_OUTPUT_FORMAT;
        }

        if (!format.equals(MimeType.SVG) && !format.equals(MimeType.PNG)) {
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
        } catch (ParseException e) {
            throw new IOException(e);
        }

        StyleConfig style = getStyleConfig(iArgs);
        String title = iArgs.getTitle();

        String result = generateSVG(title, style, new String(request.data, StandardCharsets.UTF_8));
        byte[] responseData = result.getBytes(StandardCharsets.UTF_8);

        // transcode SVG to PNG if needed
        try {
            if (format.equals(MimeType.PNG)) {
                responseData = SVGTranscoder.svg2Png(result);
            }
        } catch (TranscoderException e) {
            throw new IOException(e);
        }

        return new ResponseData(
                format,
                responseData
        );
    }
}
