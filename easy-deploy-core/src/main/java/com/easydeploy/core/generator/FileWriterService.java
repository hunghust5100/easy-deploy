package com.easydeploy.core.generator;

import com.easydeploy.core.model.ProjectConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileWriterService {

    private final TemplateRenderEngine renderEngine;

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

    public void generateAll(ProjectConfig config, Path targetDir) throws Exception {
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // 1. Generate Dockerfile
        String templateName = resolveDockerfileTemplate(config.getTechStack());
        String dockerfileContent = renderEngine.renderTemplate(templateName, config);
        writeFile(targetDir.resolve("Dockerfile"), dockerfileContent);

        // 2. Generate .dockerignore
        String dockerignoreContent = renderEngine.renderTemplate("docker/.dockerignore.ftl", config);
        writeFile(targetDir.resolve(".dockerignore"), dockerignoreContent);

        // 3. Generate docker-compose.yml
        String composeContent = renderEngine.renderTemplate("docker/docker-compose.ftl", config);
        writeFile(targetDir.resolve("docker-compose.yml"), composeContent);

        // 4. Generate nginx.conf (if enabled)
        if (config.isEnableNginx()) {
            String nginxContent = renderEngine.renderTemplate("nginx/nginx.ftl", config);
            writeFile(targetDir.resolve("nginx.conf"), nginxContent);
        }

        // 5. Generate GitHub Actions CI/CD (if enabled)
        if (config.isEnableCicd()) {
            Path workflowDir = targetDir.resolve(".github/workflows");
            if (!Files.exists(workflowDir)) {
                Files.createDirectories(workflowDir);
            }
            String cicdContent = renderEngine.renderTemplate("cicd/deploy-github.ftl", config);
            writeFile(workflowDir.resolve("deploy.yml"), cicdContent);
        }

        // 6. Generate setup-server.sh (if Server Setup enabled)
        if (config.isEnableServerSetup() || config.isInstallNginx() || config.isInstallDocker() || config.isUseDockerHub()) {
            String setupContent = renderEngine.renderTemplate("setup/setup-server.sh.ftl", config);
            writeFile(targetDir.resolve("setup-server.sh"), setupContent);
        }

        // 7. Generate README-DEPLOY.md
        String readmeContent = "# Easy Deploy Instructions\n\nRun your stack with:\n```bash\ndocker compose up -d\n```\n";
        writeFile(targetDir.resolve("README-DEPLOY.md"), readmeContent);
    }

    private void writeFile(Path filePath, String content) throws IOException {
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
