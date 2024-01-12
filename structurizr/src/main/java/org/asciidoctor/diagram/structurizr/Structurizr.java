package org.asciidoctor.diagram.structurizr;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.dsl.StructurizrDslParserAccessor;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.export.AbstractDiagramExporter;
import com.structurizr.export.Diagram;
import com.structurizr.export.dot.DOTExporter;
import com.structurizr.export.mermaid.MermaidDiagramExporter;
import com.structurizr.export.plantuml.C4PlantUMLExporter;
import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;
import com.structurizr.view.*;
import io.github.goto1134.structurizr.export.d2.D2Exporter;
import org.asciidoctor.diagram.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.stream.Stream;

public class Structurizr implements DiagramGenerator {
    static final String VIEW_HEADER = "X-Structurizr-View";

    static final MimeType PLANTUML = MimeType.parse("text/x-plantuml");
    static final MimeType PLANTUML_C4 = MimeType.parse("text/x-plantuml-c4");
    static final MimeType MERMAID = MimeType.parse("text/x-mermaid");
    static final MimeType GRAPHVIZ = MimeType.parse("text/vnd.graphviz");
    static final MimeType D2 = MimeType.parse("text/x-d2");

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
        } else if (format.isSameType(D2)) {
            exporter = new D2Exporter();
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
        String includeDir = request.headers.getValue("X-Structurizr-IncludeDir");

        try {
            Workspace workspace = parseWorkspace(request, includeDir);

            prepareWorkspaceForExport(workspace);

            View view = findView(workspace, viewKey);
            Diagram diagram = exportView(exporter, view);

            return new ResponseData(
                    format.withParameter("charset", charsetName),
                    diagram.getDefinition().getBytes(charset)
            );
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private Workspace parseWorkspace(Request request, String includeDir) throws StructurizrDslParserException, IOException {
        File baseDir;
        if (includeDir != null) {
            baseDir = new File(includeDir);
        } else {
            baseDir = new File(System.getProperty("user.dir"));
        }
        StructurizrDslParser structurizrDslParser = new StructurizrDslParser();
        StructurizrDslParserAccessor.parse(structurizrDslParser, request.asString(), baseDir);
        return structurizrDslParser.getWorkspace();
    }

    private static void prepareWorkspaceForExport(Workspace workspace) throws Exception {
        // Copied from https://github.com/structurizr/cli/blob/master/src/main/java/com/structurizr/cli/export/ExportCommand.java#L141

        // Load themes that are referenced using HTTP(S) URIs
        ThemeUtils.loadThemes(workspace);

        // Ensure at least one view exists
        if (workspace.getViews().isEmpty()) {
            workspace.getViews().createDefaultViews();
        }
    }

    private View findView(Workspace workspace, String viewKey) throws IOException {
        ViewSet viewSet = workspace.getViews();
        if (viewKey != null) {
            return viewSet.getViewWithKey(viewKey);
        }

        Stream<? extends View> views = concatStreams(
                viewSet.getCustomViews().stream(),
                viewSet.getSystemLandscapeViews().stream(),
                viewSet.getSystemContextViews().stream(),
                viewSet.getContainerViews().stream(),
                viewSet.getComponentViews().stream(),
                viewSet.getDynamicViews().stream(),
                viewSet.getDeploymentViews().stream()
        );

        return views.min(Comparator.comparing(View::getKey)).orElseThrow(() -> new IOException("Could not find view"));
    }

    @SafeVarargs
    private Stream<? extends View> concatStreams(Stream<? extends View> stream, Stream<? extends View>... streams) {
        Stream<? extends View> s = stream;
        for (Stream<? extends View> other : streams) {
            s = Stream.concat(s, other);
        }
        return s;
    }

    private Diagram exportView(AbstractDiagramExporter exporter, View view) throws IOException {
        if (view instanceof CustomView customView) {
            return exporter.export(customView);
        } else if (view instanceof SystemLandscapeView landscapeView) {
            return exporter.export(landscapeView);
        } else if (view instanceof SystemContextView contextView) {
            return exporter.export(contextView);
        } else if (view instanceof ContainerView containerView) {
            return exporter.export(containerView);
        } else if (view instanceof ComponentView componentView) {
            return exporter.export(componentView);
        } else if (view instanceof DynamicView dynamicView) {
            return exporter.export(dynamicView);
        } else if (view instanceof DeploymentView deploymentView) {
            return exporter.export(deploymentView);
        } else {
            throw new IOException("Cannot export diagram of type " + view.getClass());
        }
    }
}
