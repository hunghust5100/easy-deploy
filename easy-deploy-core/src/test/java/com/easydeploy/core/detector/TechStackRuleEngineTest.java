package com.easydeploy.core.detector;

import com.easydeploy.core.model.ProjectConfig;
import org.junit.jupiter.api.Test;
import java.util.List;

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
}
