package org.asciidoctor.diagram.plantuml;

import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.preproc.Defines;
import net.sourceforge.plantuml.security.SFile;
import org.asciidoctor.diagram.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PlantUML implements DiagramGeneratorFunction
{
    public static final String X_GRAPHVIZ = "X-Graphviz";
    public static final String X_PLANT_UML_CONFIG = "X-PlantUML-Config";
    public static final String X_PLANT_UML_DEBUG = "X-PlantUML-Debug";
    public static final String X_PLANT_UML_SIZE_LIMIT = "X-PlantUML-SizeLimit";
    public static final String X_PLANT_UML_THEME = "X-PlantUML-Theme";

    private static final MimeType DEFAULT_OUTPUT_FORMAT = MimeType.PNG;
    private static final int DEFAULT_IMAGE_SIZE_LIMIT = 4096;
    private static final String SMETANA = "smetana";
    private static final String ELK = "elk";

    private static Method SET_DOT_EXE;
    private static Object SET_DOT_EXE_INSTANCE;

    private static Method SET_LOCAL_IMAGE_LIMIT;
    private static Object SET_LOCAL_IMAGE_LIMIT_INSTANCE;

    private static Method REMOVE_LOCAL_LIMIT_SIZE;
    private static Object REMOVE_LOCAL_LIMIT_SIZE_INSTANCE;

    private static Method SET_USE_ELK;

    static {
        ClassLoader classLoader = PlantUML.class.getClassLoader();
        try {
            Class<?> optionFlags = classLoader.loadClass("net.sourceforge.plantuml.OptionFlags");
            SET_DOT_EXE = optionFlags.getMethod("setDotExecutable", String.class);
            SET_DOT_EXE_INSTANCE = optionFlags.getMethod("getInstance").invoke(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Try next option
        }

        try {
            Class<?> utils = classLoader.loadClass("net.sourceforge.plantuml.dot.GraphvizUtils");

            SET_DOT_EXE = utils.getMethod("setDotExecutable", String.class);
            SET_DOT_EXE_INSTANCE = null;

            SET_LOCAL_IMAGE_LIMIT = utils.getMethod("setLocalImageLimit", Integer.TYPE);
            SET_LOCAL_IMAGE_LIMIT_INSTANCE = null;

            REMOVE_LOCAL_LIMIT_SIZE = utils.getMethod("removeLocalLimitSize");
            REMOVE_LOCAL_LIMIT_SIZE_INSTANCE = null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Try next option
        }

        try {
            Class<?> utils = classLoader.loadClass("net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils");
            SET_DOT_EXE = utils.getMethod("setDotExecutable", String.class);
            SET_DOT_EXE_INSTANCE = null;

            SET_LOCAL_IMAGE_LIMIT = utils.getMethod("setLocalImageLimit", Integer.TYPE);
            SET_LOCAL_IMAGE_LIMIT_INSTANCE = null;

            REMOVE_LOCAL_LIMIT_SIZE = utils.getMethod("removeLocalLimitSize");
            REMOVE_LOCAL_LIMIT_SIZE_INSTANCE = null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Try next option
        }

        if (SET_DOT_EXE == null) {
            throw new IllegalStateException("Could not find setDotExecutable method");
        }

        try {
            Class<?> titledDiagram = TitledDiagram.class;
            SET_USE_ELK = titledDiagram.getMethod("setUseElk", boolean.class);
        } catch (ReflectiveOperationException | RuntimeException e) {
            SET_USE_ELK = null;
        }
    }

    @Override
    public ResponseData generate(Request request) throws IOException {
        File graphviz;

        String pathToGraphViz = request.headers.getValue(X_GRAPHVIZ);
        if (pathToGraphViz != null) {
            if (pathToGraphViz.equalsIgnoreCase(SMETANA)) {
                pathToGraphViz = SMETANA;
                graphviz = null;
            } else if (pathToGraphViz.equalsIgnoreCase(ELK)) {
                pathToGraphViz = ELK;
                graphviz = null;
            } else {
                File graphvizParam = new File(pathToGraphViz);
                if (graphvizParam.canExecute()) {
                    graphviz = graphvizParam;
                } else {
                    throw new IOException("GraphViz 'dot' tool at '" + pathToGraphViz + "' is not executable");
                }
            }
        } else {
            graphviz = null;
        }

        MimeType format = request.headers.getValue(HTTPHeader.ACCEPT);

        if (format == null) {
            format = DEFAULT_OUTPUT_FORMAT;
        }

        FileFormatOption fileFormat;
        if (format.equals(MimeType.PNG)) {
            fileFormat = new FileFormatOption(FileFormat.PNG);
        } else if (format.equals(MimeType.SVG)) {
            fileFormat = new FileFormatOption(FileFormat.SVG);
        } else if (format.equals(MimeType.TEXT_PLAIN_UTF8)) {
            fileFormat = new FileFormatOption(FileFormat.UTXT);
        } else if (format.equals(MimeType.TEXT_PLAIN)) {
            fileFormat = new FileFormatOption(FileFormat.ATXT);
            format = MimeType.parse(MimeType.TEXT_PLAIN + ";charset=" + Charset.defaultCharset().name().toLowerCase());
        } else {
            throw new IOException("Unsupported output format: " + format);
        }

        Option option = new Option();

        String plantUmlConfig = request.headers.getValue(X_PLANT_UML_CONFIG);
        if (plantUmlConfig != null) {
            option.initConfig(plantUmlConfig);
        }

        boolean debug = request.headers.getValue(X_PLANT_UML_DEBUG) != null;
        if (debug) {
            fileFormat.setDebugSvek(true);
            option.setDebugSvek(true);
        }

        option.setFileFormatOption(fileFormat);

        List<String> config = new ArrayList<>(option.getConfig());
        String plantUmlTheme = request.headers.getValue(X_PLANT_UML_THEME);
        if (plantUmlTheme != null) {
            config.add(0, "!theme " + plantUmlTheme);
        }

        int sizeLimit = DEFAULT_IMAGE_SIZE_LIMIT;
        String sizeLimitHeader = request.headers.getValue(X_PLANT_UML_SIZE_LIMIT);
        if (sizeLimitHeader != null) {
            sizeLimit = Integer.parseInt(sizeLimitHeader);
        }

        byte[] responseData = new byte[0];

        try {
            SET_LOCAL_IMAGE_LIMIT.invoke(SET_LOCAL_IMAGE_LIMIT_INSTANCE, sizeLimit);
            try {
                synchronized (this) {
                    SET_DOT_EXE.invoke(SET_DOT_EXE_INSTANCE, graphviz != null ? graphviz.getAbsolutePath() : null);

                    SFile currentDir = FileSystem.getInstance().getCurrentDir();
                    BlockUmlBuilder builder = new BlockUmlBuilder(
                            config,
                            "UTF-8",
                            Defines.createEmpty(),
                            new StringReader(request.asString()),
                            currentDir,
                            "<input>"
                    );
                    List<BlockUml> blocks = builder.getBlockUmls();

                    if (blocks.isEmpty()) {
                        throw new IOException("No @startuml found");
                    } else {
                        for (BlockUml b : blocks) {
                            Diagram system = b.getDiagram();
                            if (system instanceof PSystemError) {
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                system.exportDiagram(byteArrayOutputStream, 0, new FileFormatOption(FileFormat.UTXT));
                                String error = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
                                throw new IOException(error);
                            }

                            if (SMETANA.equalsIgnoreCase(pathToGraphViz)) {
                                if (system instanceof TitledDiagram) {
                                    ((TitledDiagram) system).setUseSmetana(true);
                                } else {
                                    throw new IOException("Cannot use Smetana engine with diagram class " + system.getClass().getSimpleName());
                                }
                            } else if (ELK.equalsIgnoreCase(pathToGraphViz)) {
                                if (system instanceof TitledDiagram) {
                                    if (SET_USE_ELK != null) {
                                        SET_USE_ELK.invoke(system, true);
                                    } else {
                                        throw new IOException("Eclipse Layout Kernel (ELK) support is not available");
                                    }
                                } else {
                                    throw new IOException("Cannot use Eclipse Layout Kernel (ELK) engine with diagram class " + system.getClass().getSimpleName());
                                }
                            }

                            if (system.getNbImages() > 0) {
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                system.exportDiagram(byteArrayOutputStream, 0, fileFormat);
                                responseData = byteArrayOutputStream.toByteArray();
                                break;
                            }
                        }

                        if (debug) {
                            MultipartWriter  multipartWriter = new MultipartWriter();
                            multipartWriter.addPart("image", format, responseData);

                            Path currentDirPath = currentDir.conv().toPath();

                            Path svekDotPath = currentDirPath.resolve("svek.dot");
                            if (Files.exists(svekDotPath)) {
                                byte[] svekDot = Files.readAllBytes(svekDotPath);
                                Files.deleteIfExists(svekDotPath);
                                multipartWriter.addPart("svekdot", MimeType.TEXT_PLAIN_UTF8, svekDot);
                            }

                            if (Files.exists(currentDirPath)) {
                                Path svekSvgPath = currentDirPath.resolve("svek.svg");
                                byte[] svekSvg = Files.readAllBytes(svekSvgPath);
                                Files.deleteIfExists(svekSvgPath);
                                multipartWriter.addPart("sveksvg", MimeType.SVG, svekSvg);
                            }

                            format = multipartWriter.getFormat();
                            responseData = multipartWriter.finish();
                        }
                    }
                }
            } finally {
                REMOVE_LOCAL_LIMIT_SIZE.invoke(REMOVE_LOCAL_LIMIT_SIZE_INSTANCE);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IOException(e);
        }

        return new ResponseData(
                format,
                responseData
        );
    }
}
