package org.asciidoctor.diagram;

import net.sourceforge.jeuclid.LayoutContext;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.layout.JEuclidView;
import net.sourceforge.jeuclid.parser.Parser;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Iterator;

class MathML implements DiagramGenerator {
    public static final MimeType DEFAULT_OUTPUT_FORMAT = MimeType.SVG;

    @Override
    public ResponseData generate(Request request) throws IOException
    {
        MimeType format = request.headers.getValue(HTTPHeader.ACCEPT);

        if (format == null) {
            format = DEFAULT_OUTPUT_FORMAT;
        }

        Converter converter;
        if (format.equals(MimeType.SVG)) {
            converter = new SVGConverter();
        } else if (format.equals(MimeType.PNG)) {
            converter = new PNGConverter();
        } else {
            throw new IOException("Unsupported output format: " + format);
        }

        String mathml = request.asString();

        Document mathmlDocument;
        try {
            mathmlDocument = Parser.getInstance().parseStreamSource(new StreamSource(new StringReader(mathml)));
        } catch (SAXException e) {
            throw new IOException(e);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        converter.convert(mathmlDocument, new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext()), out);
        out.close();

        return new ResponseData(format, out.toByteArray());
    }

    private interface Converter {
        void convert(Node node, LayoutContext ctx, OutputStream out) throws IOException;
    }

    private static class SVGConverter implements Converter {
        public void convert(Node node, LayoutContext ctx, OutputStream out) throws IOException
        {
            Document document;
            try {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            } catch (ParserConfigurationException e1) {
                throw new IOException(e1);
            }
            document.appendChild(document.createElementNS("http://www.w3.org/2000/svg", "svg"));

            SVGGeneratorContext svgCtx = SVGGeneratorContext.createDefault(document);
            svgCtx.setComment("Converted from MathML using JEuclid");

            SVGGraphics2D graphics = new SVGGraphics2D(svgCtx, true);

            JEuclidView view = new JEuclidView(node, ctx, graphics);
            int ascentHeight = (int) Math.ceil((double) view.getAscentHeight());
            int descentHeight = (int) Math.ceil((double) view.getDescentHeight());
            int height = ascentHeight + descentHeight;
            int width = (int) Math.ceil((double) view.getWidth());
            graphics.setSVGCanvasSize(new Dimension(width, height));
            view.draw(graphics, 0.0F, (float) ascentHeight);
            document.replaceChild(graphics.getRoot(), document.getFirstChild());

            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(out);
                transformer.transform(source, result);
            } catch (TransformerException e) {
                throw new IOException(e);
            }
        }
    }

    private static class PNGConverter implements Converter {
        public void convert(Node node, LayoutContext ctx, OutputStream out) throws IOException
        {
            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            JEuclidView view = new JEuclidView(node, ctx, graphics);
            int ascentHeight = (int) Math.ceil((double) view.getAscentHeight());
            int descentHeight = (int) Math.ceil((double) view.getDescentHeight());
            int height = ascentHeight + descentHeight;
            int width = (int) Math.ceil((double) view.getWidth());

            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            view.draw(graphics, 0.0F, (float) ascentHeight);

            ImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(out);

            ImageWriter writer = getImageWriter(MimeType.PNG);
            writer.setOutput(imageOutputStream);
            writer.write(image);
            imageOutputStream.close();
        }

        private ImageWriter getImageWriter(MimeType mimeType) throws IOException
        {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(mimeType.toString());
            if (!writers.hasNext()) {
                throw new IOException("Could not find ImageIO writer for " + mimeType);
            }
            return writers.next();
        }
    }
}
