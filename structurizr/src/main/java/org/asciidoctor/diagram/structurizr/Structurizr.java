package org.asciidoctor.diagram.structurizr;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.export.AbstractDiagramExporter;
import com.structurizr.export.Diagram;
import com.structurizr.export.dot.DOTExporter;
import com.structurizr.export.mermaid.MermaidDiagramExporter;
import com.structurizr.export.plantuml.C4PlantUMLExporter;
import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;
import com.structurizr.view.*;
import org.asciidoctor.diagram.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class Structurizr implements DiagramGenerator {
    static final String VIEW_HEADER = "X-Structurizr-View";

    static final MimeType PLANTUML = MimeType.parse("text/x-plantuml");
    static final MimeType PLANTUML_C4 = MimeType.parse("text/x-plantuml-c4");
    static final MimeType MERMAID = MimeType.parse("text/x-mermaid");
    static final MimeType GRAPHVIZ = MimeType.parse("text/vnd.graphviz");

    private static final MimeType DEFAULT_OUTPUT_FORMAT = PLANTUML;

    @Override
    public String getName() {
        return "structurizr";
    }

    @Override
    public ResponseData generate(Request request) throws IOException {
        MimeType format = request.headers.getValue(HTTPHeader.ACCEPT);

        if (format == null) {
            format = DEFAULT_OUTPUT_FORMAT;
        }

        AbstractDiagramExporter exporter;
        if (format.isSameType(PLANTUML_C4)) {
            exporter = new C4PlantUMLExporter();
        } else if (format.isSameType(GRAPHVIZ)) {
            exporter = new DOTExporter();
        } else if (format.isSameType(MERMAID)) {
            exporter = new MermaidDiagramExporter();
        } else if (format.isSameType(PLANTUML)) {
            exporter = new StructurizrPlantUMLExporter();
        } else {
            throw new IOException("Unsupported output format: " + format);
        }

        String charsetName = format.parameters.get("charset");
        Charset charset;
        if (charsetName == null) {
            charset = StandardCharsets.UTF_8;
            charsetName = "utf-8";
        } else {
            charset = Charset.forName(charsetName);
        }

        String viewKey = request.headers.getValue(VIEW_HEADER);

        StructurizrDslParser structurizrDslParser = new StructurizrDslParser();
        try {
            structurizrDslParser.parse(request.asString());
            Workspace workspace = structurizrDslParser.getWorkspace();
            View view = findView(workspace, viewKey);
            Diagram diagram = exportView(exporter, view);

            return new ResponseData(
                    format.withParameter("charset", charsetName),
                    diagram.getDefinition().getBytes(charset)
            );
        } catch (StructurizrDslParserException e) {
            throw new IOException(e);
        }
    }

    private View findView(Workspace workspace, String viewKey) throws IOException {
        ViewSet viewSet = workspace.getViews();

        Stream<? extends View> views = concatStreams(
                viewSet.getCustomViews().stream(),
                viewSet.getSystemLandscapeViews().stream(),
                viewSet.getSystemContextViews().stream(),
                viewSet.getContainerViews().stream(),
                viewSet.getComponentViews().stream(),
                viewSet.getDynamicViews().stream(),
                viewSet.getDeploymentViews().stream()
        );

        if (viewKey != null) {
            views = views.filter(view -> view.getKey().equals(viewKey));
        }

        return views.findFirst().orElseThrow(() -> new IOException("Could not find view with key '" + viewKey + "'"));
    }

    @SafeVarargs
    private final Stream<? extends View> concatStreams(Stream<? extends View> stream, Stream<? extends View>... streams) {
        Stream<? extends View> s = stream;
        for (Stream<? extends View> other : streams) {
            s = Stream.concat(s, other);
        }
        return s;
    }

    private Diagram exportView(AbstractDiagramExporter exporter, View view) throws IOException {
        if (view instanceof CustomView) {
            return exporter.export((CustomView) view);
        } else if (view instanceof SystemLandscapeView) {
            return exporter.export((SystemLandscapeView) view);
        } else if (view instanceof SystemContextView) {
            return exporter.export((SystemContextView) view);
        } else if (view instanceof ContainerView) {
            return exporter.export((ContainerView) view);
        } else if (view instanceof ComponentView) {
            return exporter.export((ComponentView) view);
        } else if (view instanceof DynamicView) {
            return exporter.export((DynamicView) view);
        } else if (view instanceof DeploymentView) {
            return exporter.export((DeploymentView) view);
        } else {
            throw new IOException("Cannot export diagram of type " + view.getClass());
        }
    }
}
