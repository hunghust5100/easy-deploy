package com.easydeploy.core.scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GithubTreeScanner {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GithubTreeScanner() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<String> scanGithubRepository(String repoUrl) throws Exception {
        String[] ownerAndRepo = parseGithubUrl(repoUrl);
        String owner = ownerAndRepo[0];
        String repo = ownerAndRepo[1];

        // Default to main branch, fallback to master if needed
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/git/trees/main?recursive=1", owner, repo);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Easy-Deploy-CLI")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            // Try master branch
            apiUrl = String.format("https://api.github.com/repos/%s/%s/git/trees/master?recursive=1", owner, repo);
            request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).header("Accept", "application/vnd.github.v3+json").header("User-Agent", "Easy-Deploy-CLI").GET().build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("GitHub API request failed with HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode treeNode = rootNode.get("tree");

        List<String> filePaths = new ArrayList<>();
        if (treeNode != null && treeNode.isArray()) {
            for (JsonNode item : treeNode) {
                if (item.has("path")) {
                    filePaths.add(item.get("path").asText());
                }
            }
        }

        return filePaths;
    }

    private String[] parseGithubUrl(String url) {
        String cleanUrl = url.trim().replace("https://github.com/", "").replace("http://github.com/", "");
        if (cleanUrl.endsWith(".git")) {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 4);
        }
        String[] parts = cleanUrl.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid GitHub repository URL: " + url);
        }
        return new String[]{parts[0], parts[1]};
    }
}
