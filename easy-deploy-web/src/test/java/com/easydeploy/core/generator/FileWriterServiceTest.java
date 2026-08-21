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
        config.setAppName("user-api");
        config.setDeployMode("registry_pull");
        config.setUseDockerHub(true);
        config.setDockerHubUsername("john_doe");
        config.setDockerImageTag("v2.1.0");

        FileWriterService service = new FileWriterService();
        service.generateAll(config, tempDir);

        String composeContent = Files.readString(tempDir.resolve("docker-compose.yml"));
        assertTrue(composeContent.contains("image: john_doe/user-api:v2.1.0"), "Compose must use Docker Hub image in registry_pull mode");
        assertFalse(composeContent.contains("context: ."), "Compose should not have build context in registry_pull mode");
        assertEquals("john_doe/user-api:v2.1.0", config.getFullDockerImageName());
    }

    @Test
    public void testGenerateAllTechStacks(@TempDir Path tempDir) throws Exception {
        String[] stacks = {
            "JAVA_MAVEN", "JAVA_GRADLE", "NODE_FRONTEND", "NODE_BACKEND",
            "NEXTJS_FULLSTACK", "PYTHON", "GO", "RUST", "PHP_LARAVEL",
            "DOTNET", "RUBY_RAILS"
        };

        FileWriterService service = new FileWriterService();

        for (String stack : stacks) {
            Path stackDir = tempDir.resolve(stack.toLowerCase());
            ProjectConfig config = new ProjectConfig();
            config.setAppName("app-" + stack.toLowerCase());
            config.setTechStack(stack);
            config.setAppPort(8080);
            config.setHostPort(8080);
            config.setEnableNginx(true);

            FileWriterService.GenerationSummary summary = service.generateAll(config, stackDir);
            assertNotNull(summary, "Generation summary should not be null for stack: " + stack);
            assertTrue(Files.exists(stackDir.resolve("Dockerfile")), "Dockerfile must exist for stack: " + stack);

            String dockerfile = Files.readString(stackDir.resolve("Dockerfile"));
            assertTrue(dockerfile.length() > 50, "Dockerfile content should be non-empty for stack: " + stack);
        }
    }

    @Test
    public void testGenerateMultiServiceProject(@TempDir Path tempDir) throws Exception {
        ProjectConfig config = new ProjectConfig();
        config.setAppName("easy-deploy-monorepo");
        config.setDbType("POSTGRESQL");
        config.setEnableNginx(true);

        com.easydeploy.core.model.ServiceModule webBackend = new com.easydeploy.core.model.ServiceModule(
            "easy-deploy-web", "easy-deploy-web", "easy-deploy-web",
            com.easydeploy.core.model.enums.ServiceType.BACKEND, "JAVA_GRADLE", "21", 8088, 8088
        );
        com.easydeploy.core.model.ServiceModule frontend = new com.easydeploy.core.model.ServiceModule(
            "easy-deploy-frontend", "easy-deploy-frontend", "easy-deploy-frontend",
            com.easydeploy.core.model.enums.ServiceType.FRONTEND, "NODE_FRONTEND", "20", 80, 5173
        );

        config.addService(webBackend);
        config.addService(frontend);

        FileWriterService service = new FileWriterService();
        FileWriterService.GenerationSummary summary = service.generateAll(config, tempDir);

        assertNotNull(summary);
        assertTrue(Files.exists(tempDir.resolve("easy-deploy-web/Dockerfile")), "Backend Dockerfile must exist in subfolder");
        assertTrue(Files.exists(tempDir.resolve("easy-deploy-frontend/Dockerfile")), "Frontend Dockerfile must exist in subfolder");
        assertTrue(Files.exists(tempDir.resolve("docker-compose.yml")), "Multi-service docker-compose.yml must exist");
        assertTrue(Files.exists(tempDir.resolve("nginx.conf")), "nginx.conf must exist");

        String composeContent = Files.readString(tempDir.resolve("docker-compose.yml"));
        assertTrue(composeContent.contains("easy-deploy-web:"));
        assertTrue(composeContent.contains("easy-deploy-frontend:"));
        assertTrue(composeContent.contains("context: ."));
        assertTrue(composeContent.contains("dockerfile: easy-deploy-web/Dockerfile"));
        assertTrue(composeContent.contains("context: easy-deploy-frontend"));
        assertTrue(composeContent.contains("dockerfile: Dockerfile"));
        assertTrue(composeContent.contains("db:"));
        assertTrue(composeContent.contains("nginx:"));

        String nginxContent = Files.readString(tempDir.resolve("nginx.conf"));
        assertTrue(nginxContent.contains("location /api/"));
        assertTrue(nginxContent.contains("proxy_pass http://easy-deploy-web:8088;"));
        assertTrue(nginxContent.contains("location /ws/"));
        assertTrue(nginxContent.contains("location /"));
        assertTrue(nginxContent.contains("proxy_pass http://easy-deploy-frontend:80"));
    }
}

