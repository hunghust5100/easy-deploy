package com.easydeploy.web.controller;

import com.easydeploy.core.detector.TechStackRuleEngine;
import com.easydeploy.core.model.ProjectConfig;
import com.easydeploy.core.scanner.GithubTreeScanner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/github")
public class GithubAnalyzerController {

    private final GithubTreeScanner githubTreeScanner;
    private final TechStackRuleEngine ruleEngine;

    public GithubAnalyzerController() {
        this.githubTreeScanner = new GithubTreeScanner();
        this.ruleEngine = new TechStackRuleEngine();
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeGithubRepo(@RequestBody Map<String, String> payload) {
        String repoUrl = payload.get("repoUrl");
        if (repoUrl == null || repoUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "repoUrl is required"));
        }

        try {
            List<String> filePaths = githubTreeScanner.scanGithubRepository(repoUrl);
            ProjectConfig config = ruleEngine.analyzeFilePaths(filePaths);

            return ResponseEntity.ok(Map.of(
                "repoUrl", repoUrl,
                "scannedFilesCount", filePaths.size(),
                "suggestedConfig", config
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
