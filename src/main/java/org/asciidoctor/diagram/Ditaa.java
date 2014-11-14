package org.asciidoctor.diagram;

import org.stathissideris.ascii2image.core.ConversionOptions;
import org.stathissideris.ascii2image.graphics.BitmapRenderer;
import org.stathissideris.ascii2image.text.TextGrid;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;
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
