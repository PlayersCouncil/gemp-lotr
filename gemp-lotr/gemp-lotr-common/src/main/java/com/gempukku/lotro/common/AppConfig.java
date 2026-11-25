package com.gempukku.lotro.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppConfig {
    private static final Logger LOGGER = LogManager.getLogger(AppConfig.class);
    private static Properties _properties;
    private static File _root;

    private synchronized static Properties getProperties() {
        if (_properties == null) {
            Properties props = new Properties();
            try {
                props.load(AppConfig.class.getResourceAsStream("/gemp-lotr.properties"));
                String gempPropertiesOverride = System.getProperty("gemp-lotr.override");
                if (gempPropertiesOverride != null)
                    props.load(AppConfig.class.getResourceAsStream(gempPropertiesOverride));
                for (String name : props.stringPropertyNames())
                    props.setProperty(name, resolve(props.getProperty(name), System::getenv));
                _properties = props;
            } catch (Exception exp) {
                LOGGER.error("Can't load application configuration", exp);
                throw new RuntimeException("Unable to load application configuration", exp);
            }
        }
        return _properties;
    }

    public static String getProperty(String property) {
        return getProperties().getProperty(property);
    }

    private static final Pattern FROM_ENVIRONMENT = Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*):-([^}]*)}");

    /**
     * A property's value with each ${NAME:-default} in it replaced by the environment variable NAME, or by the
     * default (which may be empty) when that variable is unset or blank. This is how docker/.env sets what
     * gemp-lotr.properties only names: every line of that file is in the app container's environment. It is the
     * shell's own form and meaning, so a line left empty in .env is as if it were not there. Nothing else in a
     * value is touched, and each property is resolved once, when the file is loaded.
     */
    static String resolve(String value, Function<String, String> environment) {
        if (value == null || !value.contains("${"))
            return value;
        Matcher placeholder = FROM_ENVIRONMENT.matcher(value);
        StringBuilder resolved = new StringBuilder();
        while (placeholder.find()) {
            String fromEnvironment = environment.apply(placeholder.group(1));
            placeholder.appendReplacement(resolved, Matcher.quoteReplacement(
                    fromEnvironment == null || fromEnvironment.isBlank() ? placeholder.group(2) : fromEnvironment));
        }
        placeholder.appendTail(resolved);
        return resolved.toString();
    }


    private static boolean AppInIDE() {
        String classPath = System.getProperty("java.class.path");
        //System.out.println("Class path: " + classPath);
        return classPath.contains("idea_rt.jar");
    }

    private static boolean AppInUnitTest() {
        //System.out.println("Stack trace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }
    public static String getResourcePath() {
        if(AppInIDE())
            return getProperties().getProperty("dev.resources.path");

        if(AppInUnitTest())
            return getProperties().getProperty("test.resources.path");

        return getProperties().getProperty("resources.path");
    }

    public static String getResourcePath(String subPath) {
        return Paths.get(getResourcePath(), subPath).toString();
    }

    public static File getResourceFile(String subPath) {
        return new File(getResourcePath(subPath));
    }

    public static FileInputStream getResourceStream(String subPath) throws IOException {
        String path = Paths.get(getResourcePath(), subPath).toString();
        return new FileInputStream(path);
    }

    public static String getWebPath() { return getProperty("web.path"); }
    public static File getCardsPath() { return getResourceFile("cards"); }
    public static File getMappingsPath() { return getResourceFile("blueprintMapping.txt"); }
    public static File getSetDefinitionsPath() { return getResourceFile("setConfig.hjson"); }
    public static File getFormatDefinitionsPath() { return getResourceFile("lotrFormats.hjson"); }
    public static File getProductPath() { return getResourceFile("product"); }
    public static File getSealedPath() { return getResourceFile("sealed"); }
    public static File getDraftPath() { return getResourceFile("draft"); }
    public static File getTableDraftPath() { return getResourceFile("draft-table"); }

}
