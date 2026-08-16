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

        // 1. Try to get repo info to find default branch
        String defaultBranch = "main";
        try {
            String repoInfoUrl = String.format("https://api.github.com/repos/%s/%s", owner, repo);
            HttpRequest infoRequest = HttpRequest.newBuilder()
                    .uri(URI.create(repoInfoUrl))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Easy-Deploy-CLI")
                    .GET()
                    .build();
            HttpResponse<String> infoResponse = httpClient.send(infoRequest, HttpResponse.BodyHandlers.ofString());
            if (infoResponse.statusCode() == 200) {
                JsonNode infoNode = objectMapper.readTree(infoResponse.body());
                if (infoNode.has("default_branch") && !infoNode.get("default_branch").isNull()) {
                    defaultBranch = infoNode.get("default_branch").asText();
                }
            }
        } catch (Exception ignored) {
            // fallback to defaultBranch = "main"
        }

        // 2. Fetch git tree with recursive=1
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/git/trees/%s?recursive=1", owner, repo, defaultBranch);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Easy-Deploy-CLI")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404 && !defaultBranch.equals("master")) {
            // Try master branch as fallback
            apiUrl = String.format("https://api.github.com/repos/%s/%s/git/trees/master?recursive=1", owner, repo);
            request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).header("Accept", "application/vnd.github.v3+json").header("User-Agent", "Easy-Deploy-CLI").GET().build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() != 200) {
            if (response.statusCode() == 404) {
                throw new RuntimeException("Không tìm thấy repository '" + owner + "/" + repo + "' hoặc repo là Private (yêu cầu Public).");
            } else if (response.statusCode() == 403) {
                throw new RuntimeException("GitHub API bị giới hạn lượt gọi (Rate Limited). Vui lòng thử lại sau giây lát.");
            }
            throw new RuntimeException("GitHub API trả về mã lỗi HTTP " + response.statusCode());
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

    public String[] parseGithubUrl(String url) {
        String cleanUrl = url.trim().replace("https://github.com/", "").replace("http://github.com/", "");
        if (cleanUrl.endsWith(".git")) {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 4);
        }
        String[] parts = cleanUrl.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Định dạng GitHub repository URL không hợp lệ: " + url);
        }
        return new String[]{parts[0], parts[1]};
    }
}
