package com.easydeploy.core.generator;

import com.easydeploy.core.model.ProjectConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileWriterServiceTest {

    @Test
    public void testGenerateAllStandardFiles(@TempDir Path tempDir) throws Exception {
        ProjectConfig config = new ProjectConfig();
        config.setAppName("demo-app");
        config.setTechStack("JAVA_GRADLE");
        config.setDbType("POSTGRESQL");
        config.setEnableNginx(true);
        config.setEnableCicd(true);
        config.setEnableServerSetup(true);

        FileWriterService service = new FileWriterService();
        FileWriterService.GenerationSummary summary = service.generateAll(config, tempDir);

        assertNotNull(summary);
        assertTrue(summary.getFiles().size() >= 7);

        assertTrue(Files.exists(tempDir.resolve("Dockerfile")), "Dockerfile must exist");
        assertTrue(Files.exists(tempDir.resolve(".dockerignore")), ".dockerignore must exist");
        assertTrue(Files.exists(tempDir.resolve("docker-compose.yml")), "docker-compose.yml must exist");
        assertTrue(Files.exists(tempDir.resolve(".env")), ".env must exist");
        assertTrue(Files.exists(tempDir.resolve(".env.example")), ".env.example must exist");
        assertTrue(Files.exists(tempDir.resolve("nginx.conf")), "nginx.conf must exist");
        assertTrue(Files.exists(tempDir.resolve(".github/workflows/deploy.yml")), ".github/workflows/deploy.yml must exist");
        assertTrue(Files.exists(tempDir.resolve("setup-server.sh")), "setup-server.sh must exist");
        assertTrue(Files.exists(tempDir.resolve("README-DEPLOY.md")), "README-DEPLOY.md must exist");

        // Verify .env content
        String envContent = Files.readString(tempDir.resolve(".env"));
        assertTrue(envContent.contains("APP_NAME=demo-app"));
        assertTrue(envContent.contains("POSTGRES_DB=app_db"));
    }

    @Test
    public void testSafeOverwriteAndBackup(@TempDir Path tempDir) throws Exception {
        ProjectConfig config = new ProjectConfig();
        config.setAppName("test-app");

        FileWriterService service = new FileWriterService();

        // 1st generation
        service.generateAll(config, tempDir, true, false);
        Path dockerfilePath = tempDir.resolve("Dockerfile");
        Files.writeString(dockerfilePath, "CUSTOM DOCKERFILE CONTENT");

        // 2nd generation without force
        FileWriterService.GenerationSummary skippedSummary = service.generateAll(config, tempDir, false, false);
        assertTrue(skippedSummary.getFiles().stream().anyMatch(f -> f.getRelativePath().equals("Dockerfile") && f.getStatus().equals("SKIPPED")));
        assertEquals("CUSTOM DOCKERFILE CONTENT", Files.readString(dockerfilePath));

        // 3rd generation with force and backup
        FileWriterService.GenerationSummary backupSummary = service.generateAll(config, tempDir, true, true);
        assertTrue(backupSummary.getFiles().stream().anyMatch(f -> f.getRelativePath().equals("Dockerfile.bak")));
        assertTrue(Files.exists(tempDir.resolve("Dockerfile.bak")));
        assertEquals("CUSTOM DOCKERFILE CONTENT", Files.readString(tempDir.resolve("Dockerfile.bak")));
    }

    @Test
    public void testDockerHubRegistryPullMode(@TempDir Path tempDir) throws Exception {
        ProjectConfig config = new ProjectConfig();
        config.setAppName("microservice");
        config.setDeployMode("registry_pull");
        config.setUseDockerHub(true);
        config.setDockerHubUsername("john_doe");
        config.setDockerImageTag("v2.1.0");

        FileWriterService service = new FileWriterService();
        service.generateAll(config, tempDir);

        String composeContent = Files.readString(tempDir.resolve("docker-compose.yml"));
        assertTrue(composeContent.contains("image: john_doe/microservice:v2.1.0"), "Compose must use Docker Hub image in registry_pull mode");
        assertFalse(composeContent.contains("context: ."), "Compose should not have build context in registry_pull mode");
        assertEquals("john_doe/microservice:v2.1.0", config.getFullDockerImageName());
    }
}

