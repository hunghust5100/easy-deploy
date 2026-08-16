package com.easydeploy.core.generator;

import com.easydeploy.core.model.ProjectConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileWriterService {

    private final TemplateRenderEngine renderEngine;

    public static class GeneratedFile {
        private final String relativePath;
        private final String status; // CREATED, OVERWRITTEN, BACKED_UP, SKIPPED

        public GeneratedFile(String relativePath, String status) {
            this.relativePath = relativePath;
            this.status = status;
        }

        public String getRelativePath() { return relativePath; }
        public String getStatus() { return status; }
    }

    public static class GenerationSummary {
        private final List<GeneratedFile> files = new ArrayList<>();

        public void addFile(String path, String status) {
            files.add(new GeneratedFile(path, status));
        }

        public List<GeneratedFile> getFiles() {
            return Collections.unmodifiableList(files);
        }
    }

    public FileWriterService() {
        this.renderEngine = new TemplateRenderEngine();
    }

    private String resolveDockerfileTemplate(String techStack) {
        if (techStack == null) return "docker/Dockerfile-java-maven.ftl";
        switch (techStack) {
            case "JAVA_GRADLE": return "docker/Dockerfile-java-gradle.ftl";
            case "NODE_FRONTEND": return "docker/Dockerfile-node-frontend.ftl";
            case "NODE_BACKEND": return "docker/Dockerfile-node-backend.ftl";
            case "PYTHON": return "docker/Dockerfile-python.ftl";
            case "JAVA_MAVEN":
            default: return "docker/Dockerfile-java-maven.ftl";
        }
    }

    public GenerationSummary generateAll(ProjectConfig config, Path targetDir) throws Exception {
        return generateAll(config, targetDir, true, false);
    }

    public GenerationSummary generateAll(ProjectConfig config, Path targetDir, boolean forceOverwrite, boolean backupIfExists) throws Exception {
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        GenerationSummary summary = new GenerationSummary();

        // 1. Generate Dockerfile
        String templateName = resolveDockerfileTemplate(config.getTechStack());
        String dockerfileContent = renderEngine.renderTemplate(templateName, config);
        writeFileWithStatus(targetDir.resolve("Dockerfile"), "Dockerfile", dockerfileContent, forceOverwrite, backupIfExists, summary);

        // 2. Generate .dockerignore
        String dockerignoreContent = renderEngine.renderTemplate("docker/.dockerignore.ftl", config);
        writeFileWithStatus(targetDir.resolve(".dockerignore"), ".dockerignore", dockerignoreContent, forceOverwrite, backupIfExists, summary);

        // 3. Generate docker-compose.yml
        String composeContent = renderEngine.renderTemplate("docker/docker-compose.ftl", config);
        writeFileWithStatus(targetDir.resolve("docker-compose.yml"), "docker-compose.yml", composeContent, forceOverwrite, backupIfExists, summary);

        // 4. Generate .env & .env.example
        String envContent = renderEngine.renderTemplate("env/.env.ftl", config);
        writeFileWithStatus(targetDir.resolve(".env"), ".env", envContent, forceOverwrite, backupIfExists, summary);

        String envExampleContent = renderEngine.renderTemplate("env/.env.example.ftl", config);
        writeFileWithStatus(targetDir.resolve(".env.example"), ".env.example", envExampleContent, forceOverwrite, backupIfExists, summary);

        // 5. Generate nginx.conf (if enabled)
        if (config.isEnableNginx()) {
            String nginxContent = renderEngine.renderTemplate("nginx/nginx.ftl", config);
            writeFileWithStatus(targetDir.resolve("nginx.conf"), "nginx.conf", nginxContent, forceOverwrite, backupIfExists, summary);
        }

        // 6. Generate GitHub Actions CI/CD (if enabled)
        if (config.isEnableCicd()) {
            Path workflowDir = targetDir.resolve(".github/workflows");
            if (!Files.exists(workflowDir)) {
                Files.createDirectories(workflowDir);
            }
            String cicdContent = renderEngine.renderTemplate("cicd/deploy-github.ftl", config);
            writeFileWithStatus(workflowDir.resolve("deploy.yml"), ".github/workflows/deploy.yml", cicdContent, forceOverwrite, backupIfExists, summary);
        }

        // 7. Generate setup-server.sh
        String setupContent = renderEngine.renderTemplate("setup/setup-server.sh.ftl", config);
        Path setupScript = targetDir.resolve("setup-server.sh");
        writeFileWithStatus(setupScript, "setup-server.sh", setupContent, forceOverwrite, backupIfExists, summary);
        try {
            setupScript.toFile().setExecutable(true, false);
        } catch (Exception ignored) {}

        // 8. Generate README-DEPLOY.md
        String readmeContent = "# 🚀 " + config.getAppName() + " — DevOps Deployment Guide\n\n"
                + "## 1. Quick Start Locally\n```bash\ndocker compose up -d --build\n```\n\n"
                + "## 2. Check Logs\n```bash\ndocker compose logs -f app\n```\n\n"
                + "## 3. Deploy to VPS (1-Click CLI)\n```bash\neasy-deploy deploy --host <vps_ip> --user root\n```\n";
        writeFileWithStatus(targetDir.resolve("README-DEPLOY.md"), "README-DEPLOY.md", readmeContent, forceOverwrite, backupIfExists, summary);

        return summary;
    }

    private void writeFileWithStatus(Path targetFile, String relativePath, String content, boolean forceOverwrite, boolean backupIfExists, GenerationSummary summary) throws IOException {
        if (Files.exists(targetFile)) {
            if (!forceOverwrite) {
                summary.addFile(relativePath, "SKIPPED");
                return;
            }
            if (backupIfExists) {
                Path backupFile = targetFile.resolveSibling(targetFile.getFileName().toString() + ".bak");
                Files.copy(targetFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
                summary.addFile(relativePath + ".bak", "BACKED_UP");
            }
            Files.writeString(targetFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            summary.addFile(relativePath, "OVERWRITTEN");
        } else {
            Files.writeString(targetFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            summary.addFile(relativePath, "CREATED");
        }
    }
}
