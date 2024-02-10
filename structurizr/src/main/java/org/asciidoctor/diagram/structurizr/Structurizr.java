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
import io.github.goto1134.structurizr.export.d2.D2Exporter;
import org.asciidoctor.diagram.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Structurizr implements DiagramGeneratorFunction {
    @FunctionalInterface
    interface ParseDsl {
        void parse(StructurizrDslParser parser, String dsl, File file) throws StructurizrDslParserException;
    }

    private static final ParseDsl PARSE = createParseDsl();

    static ParseDsl createParseDsl() {
        Class<StructurizrDslParser> parserClass = StructurizrDslParser.class;

        // Public API added in Structurizr 2.0.0
        try {
            Method parseString = parserClass.getDeclaredMethod("parse", String.class, File.class);
            return (parser, dsl, file) -> {
                try {
                    parseString.invoke(parser, dsl, file);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    if (targetException instanceof StructurizrDslParserException) {
                        throw (StructurizrDslParserException)targetException;
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            };
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Try next
        }

        // Internal API in Structurizr < 2.0.0
        try {
            Method parseString = parserClass.getDeclaredMethod("parse", List.class, File.class);
            parseString.setAccessible(true);
            return (parser, dsl, file) -> {
                List<String> lines = Arrays.asList(dsl.split("\\r?\\n"));

                try {
                    parseString.invoke(parser, lines, file);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    if (targetException instanceof StructurizrDslParserException) {
                        throw (StructurizrDslParserException)targetException;
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            };
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Try next
        }

        throw new IllegalStateException("Could not find DSL parsing method");
    }

    static final String VIEW_HEADER = "X-Structurizr-View";

    static final MimeType PLANTUML = MimeType.parse("text/x-plantuml");
    static final MimeType PLANTUML_C4 = MimeType.parse("text/x-plantuml-c4");
    static final MimeType MERMAID = MimeType.parse("text/x-mermaid");
    static final MimeType GRAPHVIZ = MimeType.parse("text/vnd.graphviz");
    static final MimeType D2 = MimeType.parse("text/x-d2");

    private static final MimeType DEFAULT_OUTPUT_FORMAT = PLANTUML;

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
        PARSE.parse(structurizrDslParser, request.asString(), new File(baseDir, "stdin"));
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
