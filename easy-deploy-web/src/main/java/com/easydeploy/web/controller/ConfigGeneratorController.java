package com.easydeploy.web.controller;

import com.easydeploy.core.generator.TemplateRenderEngine;
import com.easydeploy.core.model.ProjectConfig;
import com.easydeploy.core.model.ServiceModule;
import com.easydeploy.core.model.enums.TechStack;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private String resolveDockerfileTemplate(String techStackStr) {
        TechStack stack = TechStack.fromString(techStackStr);
        switch (stack) {
            case JAVA_GRADLE: return "docker/Dockerfile-java-gradle.ftl";
            case NODE_FRONTEND: return "docker/Dockerfile-node-frontend.ftl";
            case NODE_BACKEND: return "docker/Dockerfile-node-backend.ftl";
            case NEXTJS_FULLSTACK: return "docker/Dockerfile-nextjs.ftl";
            case PYTHON: return "docker/Dockerfile-python.ftl";
            case GO: return "docker/Dockerfile-go.ftl";
            case RUST: return "docker/Dockerfile-rust.ftl";
            case PHP_LARAVEL: return "docker/Dockerfile-php.ftl";
            case DOTNET: return "docker/Dockerfile-dotnet.ftl";
            case RUBY_RAILS: return "docker/Dockerfile-ruby.ftl";
            case JAVA_MAVEN:
            default: return "docker/Dockerfile-java-maven.ftl";
        }
    }

    private ProjectConfig createConfigForService(ProjectConfig base, ServiceModule sm) {
        ProjectConfig copy = new ProjectConfig();
        copy.setAppName(sm.getName());
        copy.setTechStack(sm.getTechStack());
        copy.setTechVersion(sm.getTechVersion());
        copy.setAppPort(sm.getContainerPort());
        copy.setHostPort(sm.getHostPort());
        copy.setDbType(base.getDbType());
        copy.setDbName(base.getDbName());
        copy.setDbUser(base.getDbUser());
        copy.setDbPass(base.getDbPass());
        copy.setDbPort(base.getDbPort());
        copy.setEnableNginx(base.isEnableNginx());
        copy.setDomainName(base.getDomainName());
        copy.setDeployMode(base.getDeployMode());
        copy.setDockerHubUsername(base.getDockerHubUsername());
        copy.setDockerImageTag(base.getDockerImageTag());

        Map<String, String> mergedEnv = new HashMap<>(base.getEnvVars());
        if (sm.getEnvVars() != null) {
            mergedEnv.putAll(sm.getEnvVars());
        }
        copy.setEnvVars(mergedEnv);
        return copy;
    }

    private void sanitizeConfig(ProjectConfig config) {
        if (config == null) return;
        if (config.getTechStack() != null && config.getTechStack().toUpperCase().contains("JAVA")) {
            config.setTechVersion(sanitizeJavaVersion(config.getTechVersion()));
        }
        if (config.getServices() != null) {
            for (ServiceModule sm : config.getServices()) {
                if (sm.getTechStack() != null && sm.getTechStack().toUpperCase().contains("JAVA")) {
                    sm.setTechVersion(sanitizeJavaVersion(sm.getTechVersion()));
                }
            }
        }
    }

    private String sanitizeJavaVersion(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "21";
        String s = raw.trim();
        if ("8".equals(s) || "1.8".equals(s) || "8.0".equals(s)) return "8";
        if ("11".equals(s) || "11.0".equals(s)) return "11";
        if ("17".equals(s) || "17.0".equals(s)) return "17";
        if ("21".equals(s) || "21.0".equals(s)) return "21";
        try {
            int v = Integer.parseInt(s.replaceAll("[^0-9]", ""));
            if (v <= 8) return "8";
            if (v <= 11) return "11";
            if (v <= 17) return "17";
            return "21";
        } catch (Exception e) {
            return "21";
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<Map<String, String>> previewConfig(@RequestBody ProjectConfig config) {
        try {
            sanitizeConfig(config);
            Map<String, String> previewMap = new LinkedHashMap<>();

            // 1. Dockerfile(s)
            if (config.hasMultipleServices()) {
                for (ServiceModule sm : config.getEnabledServices()) {
                    String dockerfileTemplate = resolveDockerfileTemplate(sm.getTechStack());
                    ProjectConfig serviceCfg = createConfigForService(config, sm);
                    String dockerfile = renderEngine.renderTemplate(dockerfileTemplate, serviceCfg);
                    String key = ".".equals(sm.getRelativePath()) ? "Dockerfile" : sm.getRelativePath() + "/Dockerfile";
                    previewMap.put(key, dockerfile);
                }
            } else {
                String dockerfileTemplate = resolveDockerfileTemplate(config.getTechStack());
                String dockerfile = renderEngine.renderTemplate(dockerfileTemplate, config);
                previewMap.put("Dockerfile", dockerfile);
            }

            // 2. docker-compose.yml
            String compose = renderEngine.renderTemplate("docker/docker-compose.ftl", config);
            previewMap.put("docker-compose.yml", compose);

            // 3. .env & .env.example
            String envContent = renderEngine.renderTemplate("env/.env.ftl", config);
            previewMap.put(".env", envContent);

            String envExample = renderEngine.renderTemplate("env/.env.example.ftl", config);
            previewMap.put(".env.example", envExample);

            // 4. .dockerignore
            String dockerignore = renderEngine.renderTemplate("docker/.dockerignore.ftl", config);
            previewMap.put(".dockerignore", dockerignore);

            // 5. nginx.conf (Nếu bật)
            if (config.isEnableNginx()) {
                String nginx = renderEngine.renderTemplate("nginx/nginx.ftl", config);
                previewMap.put("nginx.conf", nginx);
            }

            // 6. CI/CD (Nếu bật)
            if (config.isEnableCicd()) {
                String cicd = renderEngine.renderTemplate("cicd/deploy-github.ftl", config);
                previewMap.put("deploy.yml", cicd);
            }

            // 7. setup-server.sh
            String setupContent = renderEngine.renderTemplate("setup/setup-server.sh.ftl", config);
            previewMap.put("setup-server.sh", setupContent);

            return ResponseEntity.ok(previewMap);
        } catch (Exception e) {
            String err = e.getMessage() != null ? e.getMessage() : "Lỗi không xác định khi sinh mã xem trước";
            return ResponseEntity.internalServerError().body(Map.of("error", err));
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateZipPackage(@RequestBody ProjectConfig config) {
        try {
            sanitizeConfig(config);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {

                // 1. Dockerfile(s)
                if (config.hasMultipleServices()) {
                    for (ServiceModule sm : config.getEnabledServices()) {
                        String dockerfileTemplate = resolveDockerfileTemplate(sm.getTechStack());
                        ProjectConfig serviceCfg = createConfigForService(config, sm);
                        String dockerfile = renderEngine.renderTemplate(dockerfileTemplate, serviceCfg);
                        String path = ".".equals(sm.getRelativePath()) ? "Dockerfile" : sm.getRelativePath() + "/Dockerfile";
                        addZipEntry(zos, path, dockerfile);
                    }
                } else {
                    String dockerfileTemplate = resolveDockerfileTemplate(config.getTechStack());
                    String dockerfile = renderEngine.renderTemplate(dockerfileTemplate, config);
                    addZipEntry(zos, "Dockerfile", dockerfile);
                }

                // 2. .dockerignore
                String dockerignore = renderEngine.renderTemplate("docker/.dockerignore.ftl", config);
                addZipEntry(zos, ".dockerignore", dockerignore);

                // 3. docker-compose.yml
                String compose = renderEngine.renderTemplate("docker/docker-compose.ftl", config);
                addZipEntry(zos, "docker-compose.yml", compose);

                // 4. .env & .env.example
                String envContent = renderEngine.renderTemplate("env/.env.ftl", config);
                addZipEntry(zos, ".env", envContent);

                String envExample = renderEngine.renderTemplate("env/.env.example.ftl", config);
                addZipEntry(zos, ".env.example", envExample);

                // 5. nginx.conf
                if (config.isEnableNginx()) {
                    String nginx = renderEngine.renderTemplate("nginx/nginx.ftl", config);
                    addZipEntry(zos, "nginx.conf", nginx);
                }

                // 6. GitHub Actions CI/CD Pipeline
                if (config.isEnableCicd()) {
                    String cicd = renderEngine.renderTemplate("cicd/deploy-github.ftl", config);
                    addZipEntry(zos, ".github/workflows/deploy.yml", cicd);
                }

                // 7. setup-server.sh
                String setupContent = renderEngine.renderTemplate("setup/setup-server.sh.ftl", config);
                addZipEntry(zos, "setup-server.sh", setupContent);

                // 8. README-DEPLOY.md
                String readme = "# Easy Deploy Instructions for " + config.getAppName() + "\n\nRun your stack locally with:\n```bash\ndocker compose up -d --build\n```\n\nDeploy to VPS:\n```bash\neasy-deploy deploy --host <vps_ip> --user root\n```\n";
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
                if (!files.containsKey("README-DEPLOY.md")) {
                    String readme = "# Easy Deploy Custom Configuration Package\n\nRun your stack with:\n```bash\ndocker compose up -d\n```\n";
                    addZipEntry(zos, "README-DEPLOY.md", readme);
                }
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
