package com.easydeploy.web.controller;

import com.easydeploy.core.detector.TechStackRuleEngine;
import com.easydeploy.core.model.ProjectConfig;
import com.easydeploy.core.scanner.GithubTreeScanner;
import com.easydeploy.core.scanner.GithubTreeScanner.ScanResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập đường dẫn GitHub repository (repoUrl)."));
        }

        try {
            ScanResult scanResult = githubTreeScanner.scanGithubRepositoryWithDetails(repoUrl);
            ProjectConfig config = ruleEngine.analyzeGithubScan(scanResult);

            GithubTreeScanner.ParsedGithubUrl parsed = githubTreeScanner.parseGithubUrlDetailed(repoUrl);
            config.setRepoUrl(parsed.getCleanCloneUrl());
            config.setGitBranch(parsed.getBranch() != null ? parsed.getBranch() : scanResult.getDefaultBranch());

            // Extract repo name and owner from repoUrl
            try {
                String owner = scanResult.getOwner();
                String repo = scanResult.getRepo();
                String cleanAppName = repo.toLowerCase().replaceAll("[^a-z0-9_-]", "-");
                config.setAppName(cleanAppName);
                config.setDockerHubUser(owner);
                config.setDockerHubUsername(owner);
                config.setDeployPath("/root/" + cleanAppName);
            } catch (Exception ignored) {}

            return ResponseEntity.ok(Map.of(
                "repoUrl", repoUrl,
                "scannedFilesCount", scanResult.getFilePaths().size(),
                "suggestedConfig", config
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
