package org.asciidoctor.diagram.ditaa;

import org.asciidoctor.diagram.*;
import org.stathissideris.ascii2image.core.CommandLineConverter;
import org.stathissideris.ascii2image.core.ConversionOptions;
import org.stathissideris.ascii2image.core.RenderingOptions;
import org.stathissideris.ascii2image.graphics.BitmapRenderer;
import org.stathissideris.ascii2image.graphics.Diagram;
import org.stathissideris.ascii2image.graphics.SVGRenderer;
import org.stathissideris.ascii2image.text.TextGrid;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Ditaa implements DiagramGenerator
{
    public static final MimeType DEFAULT_OUTPUT_FORMAT = MimeType.PNG;
    public static final MimeType DEFAULT_CONTENT_TYPE = MimeType.TEXT_PLAIN_UTF8;
    public static final String DEFAULT_CHARSET = "UTF-8";

    @Override
    public String getName()
    {
        return "ditaa";
    }

    @Override
    public ResponseData generate(Request request) throws IOException {
        MimeType format = request.headers.getValue(HTTPHeader.ACCEPT);
        if (format == null) {
            format = DEFAULT_OUTPUT_FORMAT;
        }

        if (!format.equals(MimeType.PNG) && !format.equals(MimeType.SVG)) {
            throw new IOException("Unsupported output format: " + format);
        }

        java.util.List<String> options = new ArrayList();
        options.add("--encoding");
        options.add(StandardCharsets.UTF_8.name());

        if (format.equals(MimeType.SVG)) {
            options.add("--svg");
        }

        String optionString = request.headers.getValue(HTTPHeader.OPTIONS);
        if (optionString != null) {
            options.addAll(Arrays.asList(optionString.split(" ")));
        }
        ConversionOptions conversionOptions = ConversionOptions.parseCommandLineOptions(options.toArray(new String[0]));

        InputStream input = new ByteArrayInputStream(request.asString().getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        doConvert(input, output, conversionOptions);

        output.close();

        return new ResponseData(
                format,
                output.toByteArray()
        );
    }

    private static void doConvert(InputStream input, OutputStream output, ConversionOptions options) throws IOException {
        Diagram diagram = convertToImage(input, options);
        RenderingOptions.ImageType imageType = options.renderingOptions.getImageType();
        if (imageType == RenderingOptions.ImageType.SVG) {
            String content = (new SVGRenderer()).renderToImage(diagram, options.renderingOptions);
            OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);

            try {
                writer.write(content);
            } finally {
                writer.flush();
            }
        } else {
            BufferedImage image = (new BitmapRenderer()).renderToImage(diagram, options.renderingOptions);
            MemoryCacheImageOutputStream memCache = new MemoryCacheImageOutputStream(output);
            ImageIO.write(image, imageType.getFormatName(), memCache);
            memCache.flush();
        }

    }

    private static Diagram convertToImage(InputStream input, ConversionOptions options) throws IOException {
        TextGrid grid = new TextGrid();
        if (options.processingOptions.getCustomShapes() != null) {
            grid.addToMarkupTags(options.processingOptions.getCustomShapes().keySet());
        }

        grid.loadFrom(input, options.processingOptions);
        if (options.processingOptions.printDebugOutput()) {
            grid.printDebug();
        }

        return new Diagram(grid, options);
    }
}
