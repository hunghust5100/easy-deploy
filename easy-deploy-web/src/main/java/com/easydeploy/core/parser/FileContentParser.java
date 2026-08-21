package com.easydeploy.core.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileContentParser {

    private static final Pattern JAVA_VERSION_POM = Pattern.compile("<java\\.version>(.*?)</java\\.version>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVA_SOURCE_POM = Pattern.compile("<maven\\.compiler\\.source>(.*?)</maven\\.compiler\\.source>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVA_TARGET_POM = Pattern.compile("<maven\\.compiler\\.target>(.*?)</maven\\.compiler\\.target>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVA_RELEASE_POM = Pattern.compile("<maven\\.compiler\\.release>(.*?)</maven\\.compiler\\.release>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVA_RELEASE_TAG = Pattern.compile("<release>([0-9.]+)</release>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVA_SOURCE_TAG = Pattern.compile("<source>([0-9.]+)</source>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVA_TARGET_TAG = Pattern.compile("<target>([0-9.]+)</target>", Pattern.CASE_INSENSITIVE);

    private static final Pattern GRADLE_SOURCE_COMPAT = Pattern.compile("(?:sourceCompatibility|targetCompatibility)\\s*(?:=|:)\\s*['\"]?([0-9.]+|JavaVersion\\.VERSION_[0-9_]+)['\"]?", Pattern.CASE_INSENSITIVE);
    private static final Pattern GRADLE_LANGUAGE_VERSION = Pattern.compile("JavaLanguageVersion\\.of\\s*\\(\\s*([0-9]+)\\s*\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern GRADLE_OPTIONS_RELEASE = Pattern.compile("options\\.release\\s*(?:=|\\.set\\()\\s*['\"]?([0-9]+)['\"]?", Pattern.CASE_INSENSITIVE);
    private static final Pattern GRADLE_JVM_TARGET = Pattern.compile("jvmTarget\\s*(?:=|\\.set\\()\\s*['\"]?([0-9.]+)['\"]?", Pattern.CASE_INSENSITIVE);

    private static final Pattern SPRING_BOOT_PARENT_VERSION = Pattern.compile("<artifactId>spring-boot-starter-parent</artifactId>\\s*<version>([0-9]+)\\.", Pattern.CASE_INSENSITIVE);

    private static final Pattern NODE_PORT = Pattern.compile("(?:process\\.env\\.PORT\\s*\\|\\|\\s*|port\\s*=\\s*|listen\\()([0-9]{2,5})");

    public String parseJavaVersion(String fileContent) {
        if (fileContent == null || fileContent.trim().isEmpty()) return null;

        // 1. Direct Maven POM property tags
        Matcher m1 = JAVA_VERSION_POM.matcher(fileContent);
        if (m1.find()) {
            String v = normalizeJavaVersion(m1.group(1));
            if (v != null) return v;
        }

        Matcher m2 = JAVA_RELEASE_POM.matcher(fileContent);
        if (m2.find()) {
            String v = normalizeJavaVersion(m2.group(1));
            if (v != null) return v;
        }

        Matcher m3 = JAVA_SOURCE_POM.matcher(fileContent);
        if (m3.find()) {
            String v = normalizeJavaVersion(m3.group(1));
            if (v != null) return v;
        }

        Matcher m4 = JAVA_TARGET_POM.matcher(fileContent);
        if (m4.find()) {
            String v = normalizeJavaVersion(m4.group(1));
            if (v != null) return v;
        }

        Matcher mRel = JAVA_RELEASE_TAG.matcher(fileContent);
        if (mRel.find()) {
            String v = normalizeJavaVersion(mRel.group(1));
            if (v != null) return v;
        }

        Matcher mSrc = JAVA_SOURCE_TAG.matcher(fileContent);
        if (mSrc.find()) {
            String v = normalizeJavaVersion(mSrc.group(1));
            if (v != null) return v;
        }

        Matcher mTgt = JAVA_TARGET_TAG.matcher(fileContent);
        if (mTgt.find()) {
            String v = normalizeJavaVersion(mTgt.group(1));
            if (v != null) return v;
        }

        // 2. Gradle patterns
        Matcher mLang = GRADLE_LANGUAGE_VERSION.matcher(fileContent);
        if (mLang.find()) {
            String v = normalizeJavaVersion(mLang.group(1));
            if (v != null) return v;
        }

        Matcher mOptRel = GRADLE_OPTIONS_RELEASE.matcher(fileContent);
        if (mOptRel.find()) {
            String v = normalizeJavaVersion(mOptRel.group(1));
            if (v != null) return v;
        }

        Matcher mGradCompat = GRADLE_SOURCE_COMPAT.matcher(fileContent);
        if (mGradCompat.find()) {
            String v = normalizeJavaVersion(mGradCompat.group(1));
            if (v != null) return v;
        }

        Matcher mJvm = GRADLE_JVM_TARGET.matcher(fileContent);
        if (mJvm.find()) {
            String v = normalizeJavaVersion(mJvm.group(1));
            if (v != null) return v;
        }

        // 3. Fallback check for Spring Boot 3 vs Spring Boot 2 parent
        Matcher mSb = SPRING_BOOT_PARENT_VERSION.matcher(fileContent);
        if (mSb.find()) {
            int major = Integer.parseInt(mSb.group(1));
            if (major >= 3) {
                return "21"; // Spring Boot 3 supports Java 17 and 21, default to 21
            } else if (major == 2) {
                return "11";
            }
        }

        return "21"; // Default fallback
    }

    public String normalizeJavaVersion(String raw) {
        if (raw == null) return "21";
        String s = raw.trim().replace("${", "").replace("}", "").trim();

        if (s.equalsIgnoreCase("JavaVersion.VERSION_1_8") || s.equalsIgnoreCase("VERSION_1_8") || s.equals("1.8") || s.equals("8") || s.equals("8.0")) {
            return "8";
        }
        if (s.equalsIgnoreCase("JavaVersion.VERSION_11") || s.equalsIgnoreCase("VERSION_11") || s.equals("11") || s.equals("11.0")) {
            return "11";
        }
        if (s.equalsIgnoreCase("JavaVersion.VERSION_17") || s.equalsIgnoreCase("VERSION_17") || s.equals("17") || s.equals("17.0")) {
            return "17";
        }
        if (s.equalsIgnoreCase("JavaVersion.VERSION_21") || s.equalsIgnoreCase("VERSION_21") || s.equals("21") || s.equals("21.0")) {
            return "21";
        }

        String clean = s.replaceAll("[^0-9.]", "");
        if (clean.equals("1.8") || clean.equals("8")) return "8";
        if (clean.contains(".")) {
            String[] parts = clean.split("\\.");
            if (parts.length > 0 && !parts[0].equals("1") && !parts[0].isEmpty()) {
                clean = parts[0];
            }
        }

        try {
            int v = Integer.parseInt(clean);
            if (v <= 8) return "8";
            if (v <= 11) return "11";
            if (v <= 17) return "17";
            return "21"; // Fallback an toàn về JDK 21 LTS cho các phiên bản >= 21
        } catch (Exception e) {
            return "21";
        }
    }

    public Integer parseServerPort(String fileContent) {
        if (fileContent == null || fileContent.isEmpty()) return null;

        // Quét an toàn từng dòng để ngăn chặn Catastrophic Regex Backtracking (ReDoS)
        boolean inServerBlock = false;
        for (String line : fileContent.lines().toList()) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#") || trimmed.startsWith("//")) continue;

            if (trimmed.equals("server:") || trimmed.startsWith("server:\n")) {
                inServerBlock = true;
                continue;
            }
            if (inServerBlock && !line.startsWith(" ") && !line.startsWith("\t") && !trimmed.isEmpty()) {
                inServerBlock = false;
            }

            if (trimmed.startsWith("server.port") || (inServerBlock && trimmed.startsWith("port:")) || trimmed.startsWith("port:")) {
                Integer p = extractPortNumber(trimmed);
                if (p != null) return p;
            }

            if (trimmed.startsWith("PORT=") || trimmed.startsWith("PORT =") ||
                trimmed.startsWith("SERVER_PORT=") || trimmed.startsWith("SERVER_PORT =") ||
                trimmed.startsWith("APP_PORT=") || trimmed.startsWith("APP_PORT =")) {
                Integer p = extractPortNumber(trimmed);
                if (p != null) return p;
            }
        }

        Matcher m4 = NODE_PORT.matcher(fileContent);
        if (m4.find()) {
            try {
                return Integer.parseInt(m4.group(1).trim());
            } catch (NumberFormatException ignored) {}
        }

        return null;
    }

    private Integer extractPortNumber(String line) {
        // Hỗ trợ cả số trực tiếp (8080) và biến môi trường lồng (${SERVER_PORT:8080})
        Matcher m = Pattern.compile("(?::|=|\\s)\\s*['\"]?(?:\\$\\{[^:]*:)?([0-9]{2,5})").matcher(line);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (Exception ignored) {}
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
