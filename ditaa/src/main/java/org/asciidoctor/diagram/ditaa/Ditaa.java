package org.asciidoctor.diagram.ditaa;

import org.asciidoctor.diagram.*;
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

public class Ditaa implements DiagramGenerator
{
    public static final MimeType DEFAULT_OUTPUT_FORMAT = MimeType.PNG;

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

        if (!format.equals(MimeType.PNG)) {
            throw new IOException("Unsupported output format: " + format);
        }

        ConversionOptions conversionOptions;

        String optionString = request.headers.getValue(HTTPHeader.OPTIONS);
        if (optionString != null) {
            String[] options = optionString.split(" ");
            conversionOptions = ConversionOptions.parseCommandLineOptions(options);
        } else {
            conversionOptions = new ConversionOptions();
        }
        
        TextGrid textGrid = new TextGrid();
        textGrid.initialiseWithText(request.asString(), conversionOptions.processingOptions);

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
