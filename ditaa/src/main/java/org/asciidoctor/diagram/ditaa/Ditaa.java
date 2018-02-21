package org.asciidoctor.diagram.ditaa;

import org.asciidoctor.diagram.*;
import org.stathissideris.ascii2image.core.CommandLineConverter;
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

        MimeType contentType = request.headers.getValue(HTTPHeader.CONTENT_TYPE);
        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
        }

        String charset = contentType.parameters.get("charset");
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }

        java.util.List<String> options = new ArrayList();
        options.add("--encoding");
        options.add(charset);

        if (format.equals(MimeType.SVG)) {
            options.add("--svg");
        }

        ConversionOptions conversionOptions;

        String optionString = request.headers.getValue(HTTPHeader.OPTIONS);
        if (optionString != null) {
            options.addAll(Arrays.asList(optionString.split(" ")));
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CommandLineConverter.convert(
                options.toArray(new String[0]),
                new ByteArrayInputStream(request.data),
                output
        );
        output.close();

        return new ResponseData(
                format,
                output.toByteArray()
        );
    }
}
