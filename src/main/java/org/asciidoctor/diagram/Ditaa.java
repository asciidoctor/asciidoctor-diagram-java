package org.asciidoctor.diagram;

import org.stathissideris.ascii2image.core.ConversionOptions;
import org.stathissideris.ascii2image.graphics.BitmapRenderer;
import org.stathissideris.ascii2image.text.TextGrid;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

class Ditaa implements DiagramGenerator {
    public static final MimeType DEFAULT_OUTPUT_FORMAT = MimeType.PNG;

    @Override
    public ResponseData generate(Request request) throws IOException {
        MimeType format = request.headers.getValue(HTTPHeader.ACCEPT);

        if (format == null) {
            format = DEFAULT_OUTPUT_FORMAT;
        }

        if (!format.equals(MimeType.PNG)) {
            throw new IOException("Unsupported output format: " + format);
        }

        ConversionOptions conversionOptions = new ConversionOptions();

        String optionString = request.headers.getValue(HTTPHeader.OPTIONS);
        if (optionString != null) {
            String[] options = optionString.split(" ");
            for (int i = 0; i < options.length; i++) {
                String option = options[i];
                if (option.equals("--no-antialias") || option.equals("-A")) {
                    conversionOptions.renderingOptions.setAntialias(false);
                } else if (option.equals("--no-separation") || option.equals("-E")) {
                    conversionOptions.processingOptions.setPerformSeparationOfCommonEdges(false);
                } else if (option.equals("--round-corners") || option.equals("-r")) {
                    conversionOptions.processingOptions.setAllCornersAreRound(true);
                } else if (option.equals("--no-shadows") || option.equals("-S")) {
                    conversionOptions.renderingOptions.setDropShadows(false);
                } else if (option.equals("--debug") || option.equals("-d")) {
                    conversionOptions.renderingOptions.setRenderDebugLines(true);
                } else if (option.equals("--fixed-slope") || option.equals("-W")) {
                    conversionOptions.renderingOptions.setFixedSlope(true);
                } else if (option.equals("--transparent") || option.equals("-T")) {
                    conversionOptions.renderingOptions.setBackgroundColor(new Color(0, 0, 0, 0));
                } else if ((option.equals("--background") || option.equals("-b")) && i < options.length - 1) {
                    try {
                        Color backgroundColor = ConversionOptions.parseColor(options[++i]);
                        conversionOptions.renderingOptions.setBackgroundColor(backgroundColor);
                    } catch (IllegalArgumentException e) {
                        // Ignore option
                    }
                } else if ((option.equals("--scale") || option.equals("-s")) && i < options.length - 1) {
                    String scale = options[++i];
                    try {
                        float scaleFactor = Float.parseFloat(scale);
                        conversionOptions.renderingOptions.setScale(scaleFactor);
                    } catch (NumberFormatException e) {
                        // Ignore option
                    }
                } else if ((option.equals("--tabs") || option.equals("-t")) && i < options.length - 1) {
                    String tabSize = options[++i];
                    try {
                        int tabs = Integer.parseInt(tabSize);
                        conversionOptions.processingOptions.setTabSize(tabs);
                    } catch (NumberFormatException e) {
                        // Ignore option
                    }
                } else if ((option.equals("--tabs") || option.equals("-t")) && i < options.length - 1) {
                    String tabSize = options[++i];
                    try {
                        int tabs = Integer.parseInt(tabSize);
                        conversionOptions.processingOptions.setTabSize(tabs);
                    } catch (NumberFormatException e) {
                        // Ignore option
                    }
                }
            }
        }

        conversionOptions.processingOptions.setCharacterEncoding(Charsets.UTF8.name());

        TextGrid textGrid = new TextGrid();
        textGrid.loadFrom(new ByteArrayInputStream(request.asString().getBytes(Charsets.UTF8)), conversionOptions.processingOptions);

        org.stathissideris.ascii2image.graphics.Diagram diagram = new org.stathissideris.ascii2image.graphics.Diagram(textGrid, conversionOptions);
        BufferedImage image = new BitmapRenderer().renderToImage(diagram, conversionOptions.renderingOptions);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream cacheStream = new MemoryCacheImageOutputStream(out);
        ImageIO.write(image, "png", out);
        cacheStream.flush();
        return new ResponseData(
                format,
                out.toByteArray()
        );
    }
}
