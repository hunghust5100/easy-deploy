package com.easydeploy.web.controller;

import com.easydeploy.core.generator.TemplateRenderEngine;
import com.easydeploy.core.model.ProjectConfig;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/v1")
public class ConfigGeneratorController {

    private final TemplateRenderEngine renderEngine;

    public ConfigGeneratorController() {
        this.renderEngine = new TemplateRenderEngine();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "Easy-Deploy Spring Boot Backend is running"
        ));
    }

    private String resolveDockerfileTemplate(String techStack) {
        if (techStack == null) return "docker/Dockerfile-java-maven.ftl";
        switch (techStack) {
            case "JAVA_GRADLE":
                return "docker/Dockerfile-java-gradle.ftl";
            case "NODE_FRONTEND":
                return "docker/Dockerfile-node-frontend.ftl";
            case "NODE_BACKEND":
                return "docker/Dockerfile-node-backend.ftl";
            case "PYTHON":
                return "docker/Dockerfile-python.ftl";
            case "JAVA_MAVEN":
            default:
                return "docker/Dockerfile-java-maven.ftl";
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<Map<String, String>> previewConfig(@RequestBody ProjectConfig config) {
        try {
            Map<String, String> previewMap = new HashMap<>();

            // 1. Dockerfile
            String dockerfileTemplate = resolveDockerfileTemplate(config.getTechStack());
            String dockerfile = renderEngine.renderTemplate(dockerfileTemplate, config);
            previewMap.put("Dockerfile", dockerfile);

            // 2. .dockerignore
            String dockerignore = renderEngine.renderTemplate("docker/.dockerignore.ftl", config);
            previewMap.put(".dockerignore", dockerignore);

            // 3. docker-compose.yml
            String compose = renderEngine.renderTemplate("docker/docker-compose.ftl", config);
            previewMap.put("docker-compose.yml", compose);

            // 4. nginx.conf (Nếu bật)
            if (config.isEnableNginx()) {
                String nginx = renderEngine.renderTemplate("nginx/nginx.ftl", config);
                previewMap.put("nginx.conf", nginx);
            }

            // 5. CI/CD (Nếu bật)
            if (config.isEnableCicd()) {
                String cicd = renderEngine.renderTemplate("cicd/deploy-github.ftl", config);
                previewMap.put("deploy.yml", cicd);
            }

            return ResponseEntity.ok(previewMap);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateZipPackage(@RequestBody ProjectConfig config) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {

                // 1. Dockerfile
                String dockerfileTemplate = resolveDockerfileTemplate(config.getTechStack());
                String dockerfile = renderEngine.renderTemplate(dockerfileTemplate, config);
                addZipEntry(zos, "Dockerfile", dockerfile);

                // 2. .dockerignore
                String dockerignore = renderEngine.renderTemplate("docker/.dockerignore.ftl", config);
                addZipEntry(zos, ".dockerignore", dockerignore);

                // 3. docker-compose.yml
                String compose = renderEngine.renderTemplate("docker/docker-compose.ftl", config);
                addZipEntry(zos, "docker-compose.yml", compose);

                // 4. nginx.conf
                if (config.isEnableNginx()) {
                    String nginx = renderEngine.renderTemplate("nginx/nginx.ftl", config);
                    addZipEntry(zos, "nginx.conf", nginx);
                }

                // 5. GitHub Actions CI/CD Pipeline
                if (config.isEnableCicd()) {
                    String cicd = renderEngine.renderTemplate("cicd/deploy-github.ftl", config);
                    addZipEntry(zos, ".github/workflows/deploy.yml", cicd);
                }

                // 6. README-DEPLOY.md
                String readme = "# Easy Deploy Instructions\n\nRun your stack with:\n```bash\ndocker compose up -d\n```\n";
                addZipEntry(zos, "README-DEPLOY.md", readme);
            }

            byte[] zipBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename((config.getAppName() != null && !config.getAppName().isEmpty() ? config.getAppName() : "app") + "-devops-config.zip").build());

            return ResponseEntity.ok().headers(headers).body(zipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate-custom")
    public ResponseEntity<byte[]> generateCustomZipPackage(@RequestBody Map<String, String> files) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (Map.Entry<String, String> entry : files.entrySet()) {
                    String filename = entry.getKey();
                    if ("deploy.yml".equals(filename)) {
                        filename = ".github/workflows/deploy.yml";
                    }
                    addZipEntry(zos, filename, entry.getValue());
                }
                String readme = "# Easy Deploy Instructions\n\nRun your stack with:\n```bash\ndocker compose up -d\n```\n";
                addZipEntry(zos, "README-DEPLOY.md", readme);
            }

            byte[] zipBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename("custom-devops-config.zip").build());

            return ResponseEntity.ok().headers(headers).body(zipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void addZipEntry(ZipOutputStream zos, String filename, String content) throws Exception {
        ZipEntry entry = new ZipEntry(filename);
        zos.putNextEntry(entry);
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
}
