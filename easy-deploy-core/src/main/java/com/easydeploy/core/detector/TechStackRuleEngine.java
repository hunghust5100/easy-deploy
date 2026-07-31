package com.easydeploy.core.detector;

import com.easydeploy.core.model.ProjectConfig;
import java.util.List;

public class TechStackRuleEngine {

    public ProjectConfig analyzeFilePaths(List<String> relativePaths) {
        ProjectConfig config = new ProjectConfig();
        
        boolean hasPom = relativePaths.stream().anyMatch(p -> p.endsWith("pom.xml"));
        boolean hasGradle = relativePaths.stream().anyMatch(p -> p.contains("build.gradle"));
        boolean hasPackageJson = relativePaths.stream().anyMatch(p -> p.endsWith("package.json"));
        boolean hasRequirements = relativePaths.stream().anyMatch(p -> p.endsWith("requirements.txt") || p.endsWith("pyproject.toml"));
        boolean hasGoMod = relativePaths.stream().anyMatch(p -> p.endsWith("go.mod"));
        boolean hasCargo = relativePaths.stream().anyMatch(p -> p.endsWith("Cargo.toml"));

        if (hasPom) {
            config.setTechStack("JAVA_MAVEN");
            config.setTechVersion("21");
            config.setAppPort(8080);
        } else if (hasGradle) {
            config.setTechStack("JAVA_GRADLE");
            config.setTechVersion("25");
            config.setAppPort(8080);
        } else if (hasPackageJson) {
            boolean hasVite = relativePaths.stream().anyMatch(p -> p.contains("vite.config"));
            boolean hasNext = relativePaths.stream().anyMatch(p -> p.contains("next.config"));
            if (hasVite || hasNext) {
                config.setTechStack("NODE_FRONTEND");
                config.setAppPort(3000);
            } else {
                config.setTechStack("NODE_BACKEND");
                config.setAppPort(3000);
            }
        } else if (hasRequirements) {
            config.setTechStack("PYTHON");
            config.setAppPort(8000);
        } else if (hasGoMod) {
            config.setTechStack("GO");
            config.setAppPort(8080);
        } else if (hasCargo) {
            config.setTechStack("RUST");
            config.setAppPort(8080);
        }

        // Detect Database Drivers & Services
        boolean hasPostgres = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("postgres"));
        boolean hasMysql = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("mysql"));
        boolean hasMongo = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("mongo"));
        boolean hasRedis = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("redis"));

        if (hasPostgres) {
            config.setDbType("POSTGRESQL");
            config.setDbPort(5432);
        } else if (hasMysql) {
            config.setDbType("MYSQL");
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
}
