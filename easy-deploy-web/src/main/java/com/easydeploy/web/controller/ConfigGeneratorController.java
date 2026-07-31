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

    @PostMapping("/preview")
    public ResponseEntity<Map<String, String>> previewConfig(@RequestBody ProjectConfig config) {
        try {
            Map<String, String> previewMap = new HashMap<>();

            String dockerfile = renderEngine.renderTemplate("docker/Dockerfile-java.ftl", config);
            previewMap.put("Dockerfile", dockerfile);

            String compose = renderEngine.renderTemplate("docker/docker-compose.ftl", config);
            previewMap.put("docker-compose.yml", compose);

            if (config.isEnableNginx()) {
                String nginx = renderEngine.renderTemplate("nginx/nginx.ftl", config);
                previewMap.put("nginx.conf", nginx);
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
                String dockerfile = renderEngine.renderTemplate("docker/Dockerfile-java.ftl", config);
                addZipEntry(zos, "Dockerfile", dockerfile);

                // 2. docker-compose.yml
                String compose = renderEngine.renderTemplate("docker/docker-compose.ftl", config);
                addZipEntry(zos, "docker-compose.yml", compose);

                // 3. nginx.conf
                if (config.isEnableNginx()) {
                    String nginx = renderEngine.renderTemplate("nginx/nginx.ftl", config);
                    addZipEntry(zos, "nginx.conf", nginx);
                }

                // 4. README-DEPLOY.md
                String readme = "# Easy Deploy Instructions\n\nRun your stack with:\n```bash\ndocker compose up -d\n```\n";
                addZipEntry(zos, "README-DEPLOY.md", readme);
            }

            byte[] zipBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename(config.getAppName() + "-devops-config.zip").build());

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
