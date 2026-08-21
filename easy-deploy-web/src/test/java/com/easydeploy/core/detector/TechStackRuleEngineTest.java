package com.easydeploy.core.detector;

import com.easydeploy.core.model.ProjectConfig;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TechStackRuleEngineTest {

    @Test
    public void testDetectJavaSpringBootMaven() {
        TechStackRuleEngine engine = new TechStackRuleEngine();
        List<String> files = List.of(
            "pom.xml",
            "src/main/java/com/example/App.java",
            "src/main/resources/application.yml",
            "postgresql-driver.jar"
        );

        ProjectConfig config = engine.analyzeFilePaths(files);

        assertEquals("JAVA_MAVEN", config.getTechStack());
        assertEquals(8080, config.getAppPort());
        assertEquals("POSTGRESQL", config.getDbType());
        assertEquals(5432, config.getDbPort());
    }

    @Test
    public void testDetectJavaGradleMonorepoWithFrontend() {
        TechStackRuleEngine engine = new TechStackRuleEngine();
        List<String> files = List.of(
            "build.gradle",
            "settings.gradle",
            "easy-deploy-web/src/main/java/com/example/App.java",
            "easy-deploy-frontend/package.json",
            "easy-deploy-frontend/vite.config.js"
        );

        ProjectConfig config = engine.analyzeFilePaths(files);
        config = engine.enrichWithFileContents(config, Map.of(
            "build.gradle", "tasks.withType(JavaCompile) { options.release = 21 }",
            "package.json", "{\n  \"name\": \"easy-deploy-frontend\",\n  \"dependencies\": { \"react\": \"^18.2.0\" }\n}"
        ));

        assertEquals("JAVA_GRADLE", config.getTechStack());
        assertEquals("21", config.getTechVersion());
    }

    @Test
    public void testDetectNodeFrontendVite() {
        TechStackRuleEngine engine = new TechStackRuleEngine();
        List<String> files = List.of(
            "package.json",
            "vite.config.js",
            "src/App.jsx",
            "index.html"
        );

        ProjectConfig config = engine.analyzeFilePaths(files);

        assertEquals("NODE_FRONTEND", config.getTechStack());
        assertEquals(3000, config.getAppPort());
        assertTrue(config.isEnableNginx());
    }

    @Test
    public void testDetectPythonFastAPI() {
        TechStackRuleEngine engine = new TechStackRuleEngine();
        List<String> files = List.of(
            "requirements.txt",
            "main.py"
        );

        ProjectConfig config = engine.analyzeFilePaths(files);

        assertEquals("PYTHON", config.getTechStack());
        assertEquals(8000, config.getAppPort());
    }

    @Test
    public void testDetectDotnet() {
        TechStackRuleEngine engine = new TechStackRuleEngine();
        List<String> files = List.of("MyApp.csproj", "Program.cs", "appsettings.json");
        ProjectConfig config = engine.analyzeFilePaths(files);
        assertEquals("DOTNET", config.getTechStack());
        assertEquals("8.0", config.getTechVersion());
    }

    @Test
    public void testDetectPhpLaravel() {
        TechStackRuleEngine engine = new TechStackRuleEngine();
        List<String> files = List.of("composer.json", "artisan", "app/Http/Controllers/UserController.php");
        ProjectConfig config = engine.analyzeFilePaths(files);
        assertEquals("PHP_LARAVEL", config.getTechStack());
        assertEquals("8.2", config.getTechVersion());
    }

    @Test
    public void testDetectRubyRails() {
        TechStackRuleEngine engine = new TechStackRuleEngine();
        List<String> files = List.of("Gemfile", "config/routes.rb", "app/models/user.rb");
        ProjectConfig config = engine.analyzeFilePaths(files);
        assertEquals("RUBY_RAILS", config.getTechStack());
        assertEquals("3.3", config.getTechVersion());
    }

    @Test
    public void testDetectNextJsFullstack() {
        TechStackRuleEngine engine = new TechStackRuleEngine();
        List<String> files = List.of("package.json", "next.config.js", "pages/index.js");
        ProjectConfig config = engine.analyzeFilePaths(files);
        assertEquals("NEXTJS_FULLSTACK", config.getTechStack());
    }

    @Test
    public void testDetectGoAndRust() {
        TechStackRuleEngine engine = new TechStackRuleEngine();
        assertEquals("GO", engine.analyzeFilePaths(List.of("go.mod", "main.go")).getTechStack());
        assertEquals("RUST", engine.analyzeFilePaths(List.of("Cargo.toml", "src/main.rs")).getTechStack());
    }

    @Test
    public void testMultiServiceDiscovery() {
        TechStackRuleEngine engine = new TechStackRuleEngine();
        List<String> files = List.of(
            "easy-deploy-web/build.gradle",
            "easy-deploy-web/src/main/java/com/easydeploy/web/App.java",
            "easy-deploy-frontend/package.json",
            "easy-deploy-frontend/vite.config.js",
            "easy-deploy-frontend/src/App.jsx"
        );

        ProjectConfig config = engine.analyzeFilePaths(files);

        assertTrue(config.hasMultipleServices());
        assertEquals(2, config.getServices().size());

        assertTrue(config.getBackendService().isPresent());
        assertEquals("easy-deploy-web", config.getBackendService().get().getName());
        assertEquals("JAVA_GRADLE", config.getBackendService().get().getTechStack());

        assertTrue(config.getFrontendService().isPresent());
        assertEquals("easy-deploy-frontend", config.getFrontendService().get().getName());
        assertEquals("NODE_FRONTEND", config.getFrontendService().get().getTechStack());
    }

    @Test
    public void testMonorepoWithRootAndLibrarySubmodules() {
        TechStackRuleEngine engine = new TechStackRuleEngine();
        List<String> files = List.of(
            "settings.gradle",
            "build.gradle",
            "easy-deploy-core/build.gradle",
            "easy-deploy-core/src/main/java/Core.java",
            "easy-deploy-cli/build.gradle",
            "easy-deploy-cli/src/main/java/Cli.java",
            "easy-deploy-web/build.gradle",
            "easy-deploy-web/src/main/java/WebApp.java",
            "easy-deploy-frontend/package.json",
            "easy-deploy-frontend/vite.config.js"
        );

        ProjectConfig config = engine.analyzeFilePaths(files);

        // Root "." should NOT be added as a separate service because submodules exist
        assertFalse(config.getServices().stream().anyMatch(s -> ".".equals(s.getRelativePath())), "Root should not be added as a duplicate service");

        // Library (core) and CLI should be disabled by default
        assertFalse(config.getServices().stream().filter(s -> s.getName().contains("core")).findFirst().get().isEnabled(), "Core library should be disabled by default");
        assertFalse(config.getServices().stream().filter(s -> s.getName().contains("cli")).findFirst().get().isEnabled(), "CLI should be disabled by default");

        // Web and Frontend should be enabled
        assertTrue(config.getServices().stream().filter(s -> s.getName().contains("web")).findFirst().get().isEnabled(), "Web app should be enabled");
        assertTrue(config.getServices().stream().filter(s -> s.getName().contains("frontend")).findFirst().get().isEnabled(), "Frontend should be enabled");

        // Enabled services count should be 2 (web + frontend)
        assertEquals(2, config.getEnabledServices().size());
    }
}
