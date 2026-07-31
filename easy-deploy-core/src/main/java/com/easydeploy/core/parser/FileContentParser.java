package com.easydeploy.core.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileContentParser {

    private static final Pattern JAVA_VERSION_POM = Pattern.compile("<java\\.version>(.*?)</java\\.version>");
    private static final Pattern JAVA_SOURCE_POM = Pattern.compile("<maven\\.compiler\\.source>(.*?)</maven\\.compiler\\.source>");
    private static final Pattern SERVER_PORT_YML = Pattern.compile("port:\\s*([0-9]+)");
    private static final Pattern SERVER_PORT_PROP = Pattern.compile("server\\.port\\s*=\\s*([0-9]+)");

    public String parseJavaVersion(String fileContent) {
        if (fileContent == null || fileContent.isEmpty()) return null;

        Matcher m1 = JAVA_VERSION_POM.matcher(fileContent);
        if (m1.find()) return m1.group(1).trim();

        Matcher m2 = JAVA_SOURCE_POM.matcher(fileContent);
        if (m2.find()) return m2.group(1).trim();

        if (fileContent.contains("JavaLanguageVersion.of(25)") || fileContent.contains("25")) {
            return "25";
        } else if (fileContent.contains("JavaLanguageVersion.of(21)") || fileContent.contains("21")) {
            return "21";
        } else if (fileContent.contains("17")) {
            return "17";
        }

        return null;
    }

    public Integer parseServerPort(String fileContent) {
        if (fileContent == null || fileContent.isEmpty()) return null;

        Matcher m1 = SERVER_PORT_PROP.matcher(fileContent);
        if (m1.find()) {
            return Integer.parseInt(m1.group(1).trim());
        }

        Matcher m2 = SERVER_PORT_YML.matcher(fileContent);
        if (m2.find()) {
            return Integer.parseInt(m2.group(1).trim());
        }

        return null;
    }
}
