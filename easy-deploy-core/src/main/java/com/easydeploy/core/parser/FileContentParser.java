package com.easydeploy.core.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileContentParser {

    private static final Pattern JAVA_VERSION_POM = Pattern.compile("<java\\.version>(.*?)</java\\.version>");
    private static final Pattern JAVA_SOURCE_POM = Pattern.compile("<maven\\.compiler\\.source>(.*?)</maven\\.compiler\\.source>");
    private static final Pattern JAVA_TARGET_POM = Pattern.compile("<maven\\.compiler\\.target>(.*?)</maven\\.compiler\\.target>");
    private static final Pattern SERVER_PORT_YML = Pattern.compile("(?:server\\s*:\\s*\\n(?:\\s+.*\\n)*?\\s+port\\s*:\\s*|port:\\s*)([0-9]+)");
    private static final Pattern SERVER_PORT_PROP = Pattern.compile("server\\.port\\s*=\\s*([0-9]+)");
    private static final Pattern PORT_ENV = Pattern.compile("^(?:PORT|APP_PORT|SERVER_PORT)\\s*=\\s*([0-9]+)", Pattern.MULTILINE);
    private static final Pattern NODE_PORT = Pattern.compile("(?:process\\.env\\.PORT\\s*\\|\\|\\s*|port\\s*=\\s*|listen\\()([0-9]{2,5})");

    public String parseJavaVersion(String fileContent) {
        if (fileContent == null || fileContent.isEmpty()) return null;

        Matcher m1 = JAVA_VERSION_POM.matcher(fileContent);
        if (m1.find()) return m1.group(1).trim().replace("${", "").replace("}", "");

        Matcher m2 = JAVA_SOURCE_POM.matcher(fileContent);
        if (m2.find()) return m2.group(1).trim();

        Matcher m3 = JAVA_TARGET_POM.matcher(fileContent);
        if (m3.find()) return m3.group(1).trim();

        if (fileContent.contains("JavaLanguageVersion.of(25)") || fileContent.contains("options.release = 25") || fileContent.contains("sourceCompatibility = '25'")) {
            return "25";
        } else if (fileContent.contains("JavaLanguageVersion.of(21)") || fileContent.contains("options.release = 21") || fileContent.contains("sourceCompatibility = '21'")) {
            return "21";
        } else if (fileContent.contains("JavaLanguageVersion.of(17)") || fileContent.contains("options.release = 17") || fileContent.contains("sourceCompatibility = '17'")) {
            return "17";
        } else if (fileContent.contains("JavaLanguageVersion.of(11)") || fileContent.contains("sourceCompatibility = '11'")) {
            return "11";
        } else if (fileContent.contains("JavaLanguageVersion.of(8)") || fileContent.contains("sourceCompatibility = '1.8'")) {
            return "8";
        }

        return "21"; // default modern LTS
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

        Matcher m3 = PORT_ENV.matcher(fileContent);
        if (m3.find()) {
            return Integer.parseInt(m3.group(1).trim());
        }

        Matcher m4 = NODE_PORT.matcher(fileContent);
        if (m4.find()) {
            try {
                return Integer.parseInt(m4.group(1).trim());
            } catch (NumberFormatException ignored) {}
        }

        return null;
    }

    public String detectDatabaseFromContent(String content) {
        if (content == null || content.isEmpty()) return null;
        String lower = content.toLowerCase();

        if (lower.contains("org.postgresql") || lower.contains("postgresql") || lower.contains("pg") || lower.contains("psycopg2") || lower.contains("asyncpg")) {
            return "POSTGRESQL";
        } else if (lower.contains("mysql-connector") || lower.contains("com.mysql") || lower.contains("mysql2") || lower.contains("pymysql") || lower.contains("aiomysql")) {
            return "MYSQL";
        } else if (lower.contains("mariadb-java-client") || lower.contains("mariadb")) {
            return "MARIADB";
        } else if (lower.contains("mongodb") || lower.contains("mongoose") || lower.contains("spring-boot-starter-data-mongodb") || lower.contains("pymongo") || lower.contains("motor")) {
            return "MONGODB";
        } else if (lower.contains("redis") || lower.contains("jedis") || lower.contains("lettuce") || lower.contains("ioredis")) {
            return "REDIS";
        }
        return null;
    }
}
