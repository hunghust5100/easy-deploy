package com.easydeploy.core.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FileContentParserTest {

    private final FileContentParser parser = new FileContentParser();

    @Test
    public void testParseJavaVersionFromPom() {
        String pom1 = "<project><properties><java.version>17</java.version></properties></project>";
        assertEquals("17", parser.parseJavaVersion(pom1));

        String pom2 = "<project><properties><java.version>21</java.version></properties></project>";
        assertEquals("21", parser.parseJavaVersion(pom2));

        String pom3 = "<project><properties><java.version>1.8</java.version></properties></project>";
        assertEquals("8", parser.parseJavaVersion(pom3));

        String pom4 = "<project><properties><maven.compiler.source>17</maven.compiler.source><maven.compiler.target>17</maven.compiler.target></properties></project>";
        assertEquals("17", parser.parseJavaVersion(pom4));

        String pom5 = "<project><properties><maven.compiler.release>11</maven.compiler.release></properties></project>";
        assertEquals("11", parser.parseJavaVersion(pom5));

        String pom6 = "<project><parent><artifactId>spring-boot-starter-parent</artifactId><version>3.2.0</version></parent></project>";
        assertEquals("17", parser.parseJavaVersion(pom6));
    }

    @Test
    public void testParseJavaVersionFromGradle() {
        String gradle1 = "plugins { id 'java' }\nsourceCompatibility = '17'\n";
        assertEquals("17", parser.parseJavaVersion(gradle1));

        String gradle2 = "java {\n    toolchain {\n        languageVersion = JavaLanguageVersion.of(21)\n    }\n}";
        assertEquals("21", parser.parseJavaVersion(gradle2));

        String gradle3 = "sourceCompatibility = JavaVersion.VERSION_1_8\n";
        assertEquals("8", parser.parseJavaVersion(gradle3));

        String gradle4 = "compileJava {\n    options.release = 17\n}";
        assertEquals("17", parser.parseJavaVersion(gradle4));

        String gradle5 = "tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile) {\n    kotlinOptions.jvmTarget = '17'\n}";
        assertEquals("17", parser.parseJavaVersion(gradle5));
    }

    @Test
    public void testDetectDatabaseFromContent() {
        assertEquals("POSTGRESQL", parser.detectDatabaseFromContent("implementation 'org.postgresql:postgresql'"));
        assertEquals("MYSQL", parser.detectDatabaseFromContent("mysql-connector-j"));
        assertEquals("MONGODB", parser.detectDatabaseFromContent("spring-boot-starter-data-mongodb"));
        assertEquals("REDIS", parser.detectDatabaseFromContent("spring-boot-starter-data-redis"));
    }
}
