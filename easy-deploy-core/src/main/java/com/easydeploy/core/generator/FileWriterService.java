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

    public void generateAll(ProjectConfig config, Path targetDir) throws Exception {
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // 1. Generate Dockerfile
        String dockerfileContent = renderEngine.renderTemplate("docker/Dockerfile-java.ftl", config);
        writeFile(targetDir.resolve("Dockerfile"), dockerfileContent);

        // 2. Generate docker-compose.yml
        String composeContent = renderEngine.renderTemplate("docker/docker-compose.ftl", config);
        writeFile(targetDir.resolve("docker-compose.yml"), composeContent);

        // 3. Generate nginx.conf (if enabled)
        if (config.isEnableNginx()) {
            String nginxContent = renderEngine.renderTemplate("nginx/nginx.ftl", config);
            writeFile(targetDir.resolve("nginx.conf"), nginxContent);
        }

        // 4. Generate README-DEPLOY.md
        String readmeContent = "# Easy Deploy Instructions\n\nRun your stack with:\n```bash\ndocker compose up -d\n```\n";
        writeFile(targetDir.resolve("README-DEPLOY.md"), readmeContent);
    }

    private void writeFile(Path filePath, String content) throws IOException {
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
