package org.asciidoctor.diagram.plantuml;

import net.sourceforge.plantuml.FileFormatOption;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

class PlantUMLOptions {
    private static final Constructor<?> CONSTRUCTOR;
    private static final Method INIT_CONFIG;
    private static final Method SET_FILE_FORMAT_OPTION;
    private static final Method SET_DEBUG_SVEK;
    private static final Method GET_CONFIG;

    static {
        ClassLoader classLoader = PlantUMLOptions.class.getClassLoader();

        Constructor<?> constructor;
        Method initConfig;
        Method setFileFormatOption;
        Method setDebugSvek;
        Method getConfig;

        try {
            Class<?> optionClass = classLoader.loadClass("net.sourceforge.plantuml.cli.CliOptions");

            constructor = optionClass.getDeclaredConstructor();
            initConfig = optionClass.getMethod("addInConfig", String.class);
            setFileFormatOption = optionClass.getMethod("setFileFormatOption", FileFormatOption.class);
            setDebugSvek = null;
            getConfig = optionClass.getMethod("getConfig");
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Try next option
            constructor = null;
            initConfig = null;
            setFileFormatOption = null;
            setDebugSvek = null;
            getConfig = null;
        }

        if (constructor == null) {
            try {
                Class<?> optionClass = classLoader.loadClass("net.sourceforge.plantuml.Option");

                constructor = optionClass.getDeclaredConstructor();
                initConfig = optionClass.getMethod("initConfig", String.class);
                setFileFormatOption = optionClass.getMethod("setFileFormatOption", FileFormatOption.class);
                setDebugSvek = optionClass.getMethod("setDebugSvek", Boolean.TYPE);
                getConfig = optionClass.getMethod("getConfig");
            } catch (ReflectiveOperationException | RuntimeException e) {
                // Try next option
                constructor = null;
                initConfig = null;
                setFileFormatOption = null;
                setDebugSvek = null;
                getConfig = null;
            }
        }

        CONSTRUCTOR = constructor;
        INIT_CONFIG = initConfig;
        SET_FILE_FORMAT_OPTION = setFileFormatOption;
        SET_DEBUG_SVEK = setDebugSvek;
        GET_CONFIG = getConfig;
    }

    private Object options;

    public PlantUMLOptions() {
        try {
            if (CONSTRUCTOR == null) {
                throw new RuntimeException("No suitable constructor found");
            }

            this.options = CONSTRUCTOR.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void initConfig(String config) {
        try {
            INIT_CONFIG.invoke(options, config);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFileFormatOption(FileFormatOption fileFormat) {
        try {
            SET_FILE_FORMAT_OPTION.invoke(options, fileFormat);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDebugSvek(boolean debug) {
        try {
            if (SET_DEBUG_SVEK != null) {
                SET_DEBUG_SVEK.invoke(options, debug);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getConfig() {
        try {
            return (List<String>)GET_CONFIG.invoke(options);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
