package com.easydeploy.core.detector;

import com.easydeploy.core.model.ProjectConfig;
import com.easydeploy.core.parser.FileContentParser;
import com.easydeploy.core.scanner.LocalTreeScanner;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class TechStackRuleEngine {

    private final FileContentParser parser;
    private final LocalTreeScanner localScanner;

    public TechStackRuleEngine() {
        this.parser = new FileContentParser();
        this.localScanner = new LocalTreeScanner();
    }

    public ProjectConfig analyzeFilePaths(List<String> relativePaths) {
        ProjectConfig config = new ProjectConfig();
        
        boolean hasPom = relativePaths.stream().anyMatch(p -> p.endsWith("pom.xml"));
        boolean hasGradle = relativePaths.stream().anyMatch(p -> p.contains("build.gradle") || p.contains("build.gradle.kts"));
        boolean hasPackageJson = relativePaths.stream().anyMatch(p -> p.endsWith("package.json"));
        boolean hasRequirements = relativePaths.stream().anyMatch(p -> p.endsWith("requirements.txt") || p.endsWith("pyproject.toml") || p.endsWith("Pipfile"));
        boolean hasGoMod = relativePaths.stream().anyMatch(p -> p.endsWith("go.mod"));
        boolean hasCargo = relativePaths.stream().anyMatch(p -> p.endsWith("Cargo.toml"));

        if (hasPom) {
            config.setTechStack("JAVA_MAVEN");
            config.setTechVersion("21");
            config.setAppPort(8080);
            config.setHostPort(8080);
        } else if (hasGradle) {
            config.setTechStack("JAVA_GRADLE");
            config.setTechVersion("21");
            config.setAppPort(8080);
            config.setHostPort(8080);
        } else if (hasPackageJson) {
            boolean hasVite = relativePaths.stream().anyMatch(p -> p.contains("vite.config"));
            boolean hasNext = relativePaths.stream().anyMatch(p -> p.contains("next.config"));
            if (hasVite || hasNext) {
                config.setTechStack("NODE_FRONTEND");
                config.setAppPort(3000);
                config.setHostPort(80);
            } else {
                config.setTechStack("NODE_BACKEND");
                config.setAppPort(3000);
                config.setHostPort(3000);
            }
        } else if (hasRequirements) {
            config.setTechStack("PYTHON");
            config.setTechVersion("3.11");
            config.setAppPort(8000);
            config.setHostPort(8000);
        } else if (hasGoMod) {
            config.setTechStack("GO");
            config.setTechVersion("1.22");
            config.setAppPort(8080);
            config.setHostPort(8080);
        } else if (hasCargo) {
            config.setTechStack("RUST");
            config.setAppPort(8080);
            config.setHostPort(8080);
        }

        // Detect Database Drivers & Services from file path names
        boolean hasPostgres = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("postgres"));
        boolean hasMysql = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("mysql"));
        boolean hasMaria = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("mariadb"));
        boolean hasMongo = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("mongo"));
        boolean hasRedis = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("redis"));

        if (hasPostgres) {
            config.setDbType("POSTGRESQL");
            config.setDbPort(5432);
        } else if (hasMysql) {
            config.setDbType("MYSQL");
            config.setDbPort(3306);
        } else if (hasMaria) {
            config.setDbType("MARIADB");
            config.setDbPort(3306);
        } else if (hasMongo) {
            config.setDbType("MONGODB");
            config.setDbPort(27017);
        } else if (hasRedis) {
            config.setDbType("REDIS");
            config.setDbPort(6379);
        } else {
            config.setDbType("POSTGRESQL"); // Default recommendation
            config.setDbPort(5432);
        }

        config.setEnableNginx(true);
        config.setEnableCicd(true);

        return config;
    }

    /**
     * Quét sâu dự án cục bộ từ thư mục rootPath, đọc nội dung các file config để nhận diện chi tiết
     */
    public ProjectConfig analyzeLocalProject(Path rootPath) {
        try {
            List<String> filePaths = localScanner.scanLocalDirectory(rootPath, 4);
            ProjectConfig config = analyzeFilePaths(filePaths);

            // 1. Tên App từ thư mục gốc
            String folderName = rootPath.getFileName() != null ? rootPath.getFileName().toString() : "my-app";
            config.setAppName(folderName.toLowerCase().replaceAll("[^a-z0-9_-]", "-"));

            // 2. Kiểm tra Java Version & Port từ POM / Gradle / YML
            Optional<String> pomContent = localScanner.readFileIfExists(rootPath, "pom.xml");
            if (pomContent.isPresent()) {
                String v = parser.parseJavaVersion(pomContent.get());
                if (v != null) config.setTechVersion(v);
                String db = parser.detectDatabaseFromContent(pomContent.get());
                if (db != null) config.setDbType(db);
            }

            Optional<String> gradleContent = localScanner.readFileIfExists(rootPath, "build.gradle");
            if (gradleContent.isPresent()) {
                String v = parser.parseJavaVersion(gradleContent.get());
                if (v != null) config.setTechVersion(v);
                String db = parser.detectDatabaseFromContent(gradleContent.get());
                if (db != null) config.setDbType(db);
            }

            // Kiểm tra Port từ application.yml / properties
            Optional<String> appYml = localScanner.readFileIfExists(rootPath, "src/main/resources/application.yml");
            if (!appYml.isPresent()) appYml = localScanner.readFileIfExists(rootPath, "src/main/resources/application.yaml");
            if (appYml.isPresent()) {
                Integer port = parser.parseServerPort(appYml.get());
                if (port != null) {
                    config.setAppPort(port);
                    config.setHostPort(port);
                }
            }

            Optional<String> appProp = localScanner.readFileIfExists(rootPath, "src/main/resources/application.properties");
            if (appProp.isPresent()) {
                Integer port = parser.parseServerPort(appProp.get());
                if (port != null) {
                    config.setAppPort(port);
                    config.setHostPort(port);
                }
            }

            // 3. Kiểm tra package.json cho Node.js
            Optional<String> pkgJson = localScanner.readFileIfExists(rootPath, "package.json");
            if (pkgJson.isPresent()) {
                String content = pkgJson.get();
                String db = parser.detectDatabaseFromContent(content);
                if (db != null) config.setDbType(db);

                if (content.contains("\"next\"") || content.contains("\"vite\"") || content.contains("\"react\"") || content.contains("\"vue\"")) {
                    config.setTechStack("NODE_FRONTEND");
                    config.setAppPort(3000);
                    config.setHostPort(80);
                } else if (content.contains("\"express\"") || content.contains("\"@nestjs/core\"") || content.contains("\"fastify\"") || content.contains("\"koa\"")) {
                    config.setTechStack("NODE_BACKEND");
                    config.setAppPort(3000);
                    config.setHostPort(3000);
                }
            }

            // 4. Kiểm tra requirements.txt cho Python
            Optional<String> reqTxt = localScanner.readFileIfExists(rootPath, "requirements.txt");
            if (reqTxt.isPresent()) {
                String content = reqTxt.get();
                String db = parser.detectDatabaseFromContent(content);
                if (db != null) config.setDbType(db);
                if (content.contains("fastapi") || content.contains("uvicorn")) {
                    config.setAppPort(8000);
                    config.setHostPort(8000);
                } else if (content.contains("flask")) {
                    config.setAppPort(5000);
                    config.setHostPort(5000);
                } else if (content.contains("django")) {
                    config.setAppPort(8000);
                    config.setHostPort(8000);
                }
            }

            // 5. Kiểm tra .env hoặc .env.example
            Optional<String> envContent = localScanner.readFileIfExists(rootPath, ".env");
            if (!envContent.isPresent()) envContent = localScanner.readFileIfExists(rootPath, ".env.example");
            if (envContent.isPresent()) {
                Integer port = parser.parseServerPort(envContent.get());
                if (port != null) {
                    config.setAppPort(port);
                    config.setHostPort(port);
                }
            }

            return config;
        } catch (Exception e) {
            return analyzeFilePaths(List.of());
        }
    }
}
