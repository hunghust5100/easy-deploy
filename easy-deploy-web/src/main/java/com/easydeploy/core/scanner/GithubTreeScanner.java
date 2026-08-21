package com.easydeploy.core.scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GithubTreeScanner {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GithubTreeScanner() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public static class ScanResult {
        private final String owner;
        private final String repo;
        private final String defaultBranch;
        private final List<String> filePaths;
        private final Map<String, String> keyFilesContent;

        public ScanResult(String owner, String repo, String defaultBranch, List<String> filePaths, Map<String, String> keyFilesContent) {
            this.owner = owner;
            this.repo = repo;
            this.defaultBranch = defaultBranch;
            this.filePaths = filePaths;
            this.keyFilesContent = keyFilesContent;
        }

        public String getOwner() { return owner; }
        public String getRepo() { return repo; }
        public String getDefaultBranch() { return defaultBranch; }
        public List<String> getFilePaths() { return filePaths; }
        public Map<String, String> getKeyFilesContent() { return keyFilesContent; }
    }

    public List<String> scanGithubRepository(String repoUrl) throws Exception {
        ScanResult result = scanGithubRepositoryWithDetails(repoUrl);
        return result.getFilePaths();
    }

    public ScanResult scanGithubRepositoryWithDetails(String repoUrl) throws Exception {
        ParsedGithubUrl parsed = parseGithubUrlDetailed(repoUrl);
        String owner = parsed.getOwner();
        String repo = parsed.getRepo();

        String defaultBranch = (parsed.getBranch() != null && !parsed.getBranch().isEmpty()) ? parsed.getBranch() : "main";

        // 1. Ưu tiên quét trực tiếp qua Zipball (Nhanh, không phụ thuộc API token, không bị giới hạn 60 req/h)
        try {
            ScanResult zipResult = scanGithubViaZipball(owner, repo, defaultBranch);
            if (zipResult != null && !zipResult.getFilePaths().isEmpty()) {
                return zipResult;
            }
        } catch (Exception ignored) {}

        // 2. Fallback sang GitHub REST API nếu quét qua archive không thành công
        try {
            String repoInfoUrl = String.format("https://api.github.com/repos/%s/%s", owner, repo);
            HttpRequest infoRequest = HttpRequest.newBuilder()
                    .uri(URI.create(repoInfoUrl))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Easy-Deploy-CLI")
                    .timeout(Duration.ofSeconds(5))
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

        String githubToken = System.getenv("GITHUB_TOKEN");

        // 2. Fetch git tree with recursive=1
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/git/trees/%s?recursive=1", owner, repo, defaultBranch);

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Easy-Deploy-CLI")
                .timeout(Duration.ofSeconds(8))
                .GET();

        if (githubToken != null && !githubToken.trim().isEmpty()) {
            reqBuilder.header("Authorization", "Bearer " + githubToken.trim());
        }

        HttpResponse<String> response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404 && !defaultBranch.equals("master")) {
            // Try master branch as fallback
            defaultBranch = "master";
            apiUrl = String.format("https://api.github.com/repos/%s/%s/git/trees/master?recursive=1", owner, repo);
            HttpRequest.Builder masterReq = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Easy-Deploy-CLI")
                    .timeout(Duration.ofSeconds(8))
                    .GET();
            if (githubToken != null && !githubToken.trim().isEmpty()) {
                masterReq.header("Authorization", "Bearer " + githubToken.trim());
            }
            response = httpClient.send(masterReq.build(), HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() != 200) {
            if (response.statusCode() == 403 || response.statusCode() == 429) {
                // Tự động chuyển sang fallback tải và phân tích GitHub Zipball không giới hạn Rate Limit
                ScanResult zipResult = scanGithubViaZipball(owner, repo, defaultBranch);
                if (zipResult != null) {
                    return zipResult;
                }
                throw new RuntimeException("GitHub API bị giới hạn lượt gọi (Rate Limited) và không thể tải archive.");
            } else if (response.statusCode() == 404) {
                // Thử fallback zipball trong trường hợp branch khác
                ScanResult zipResult = scanGithubViaZipball(owner, repo, defaultBranch);
                if (zipResult != null) {
                    return zipResult;
                }
                throw new RuntimeException("Không tìm thấy repository '" + owner + "/" + repo + "' hoặc repo là Private (yêu cầu Public).");
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

        // 3. Fetch key files content in parallel using non-blocking sendAsync
        Map<String, String> keyFilesContent = new java.util.concurrent.ConcurrentHashMap<>();
        List<String> targetKeyFiles = List.of(
                "pom.xml",
                "build.gradle",
                "build.gradle.kts",
                "package.json",
                "requirements.txt",
                "pyproject.toml",
                "go.mod",
                "src/main/resources/application.yml",
                "src/main/resources/application.yaml",
                "src/main/resources/application.properties"
        );

        final String effectiveBranch = defaultBranch;
        List<java.util.concurrent.CompletableFuture<Void>> futures = new ArrayList<>();
        for (String target : targetKeyFiles) {
            Optional<String> matchPath = filePaths.stream()
                    .filter(p -> p.equalsIgnoreCase(target))
                    .findFirst();
            if (!matchPath.isPresent()) {
                matchPath = filePaths.stream()
                        .filter(p -> p.endsWith("/" + target))
                        .findFirst();
            }
            if (matchPath.isPresent()) {
                final String targetKey = target;
                futures.add(fetchFileContentAsync(owner, repo, effectiveBranch, matchPath.get())
                        .thenAccept(opt -> opt.ifPresent(content -> keyFilesContent.put(targetKey, content))));
            }
        }

        if (!futures.isEmpty()) {
            try {
                java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0]))
                        .orTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                        .exceptionally(ex -> null)
                        .join();
            } catch (Exception ignored) {}
        }

        return new ScanResult(owner, repo, defaultBranch, filePaths, keyFilesContent);
    }

    public java.util.concurrent.CompletableFuture<Optional<String>> fetchFileContentAsync(String owner, String repo, String defaultBranch, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return java.util.concurrent.CompletableFuture.completedFuture(Optional.empty());
        }
        try {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", owner, repo, filePath, defaultBranch);
            HttpRequest.Builder apiReq = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/vnd.github.v3.raw")
                    .header("User-Agent", "Easy-Deploy-CLI")
                    .timeout(Duration.ofSeconds(3))
                    .GET();
            String githubToken = System.getenv("GITHUB_TOKEN");
            if (githubToken != null && !githubToken.trim().isEmpty()) {
                apiReq.header("Authorization", "Bearer " + githubToken.trim());
            }

            return httpClient.sendAsync(apiReq.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(res -> {
                        if (res.statusCode() == 200 && res.body() != null && !res.body().isEmpty()) {
                            return Optional.of(res.body());
                        }
                        return Optional.<String>empty();
                    })
                    .exceptionally(ex -> Optional.empty());
        } catch (Exception e) {
            return java.util.concurrent.CompletableFuture.completedFuture(Optional.empty());
        }
    }

    public Optional<String> fetchFileContent(String owner, String repo, String defaultBranch, String filePath) {
        try {
            return fetchFileContentAsync(owner, repo, defaultBranch, filePath).join();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public ScanResult scanGithubViaZipball(String owner, String repo, String defaultBranch) {
        String[] branchesToTry = new String[]{ defaultBranch, "master", "main" };
        List<String> targetKeyFiles = List.of(
                "pom.xml",
                "build.gradle",
                "build.gradle.kts",
                "package.json",
                "requirements.txt",
                "pyproject.toml",
                "go.mod",
                "src/main/resources/application.yml",
                "src/main/resources/application.yaml",
                "src/main/resources/application.properties"
        );

        for (String branch : branchesToTry) {
            if (branch == null || branch.isEmpty()) continue;
            try {
                String zipUrl = String.format("https://github.com/%s/%s/archive/refs/heads/%s.zip", owner, repo, branch);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(zipUrl))
                        .header("User-Agent", "Easy-Deploy-CLI")
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<byte[]> zipResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (zipResponse.statusCode() == 200 && zipResponse.body() != null) {
                    List<String> filePaths = new ArrayList<>();
                    Map<String, String> keyFilesContent = new HashMap<>();

                    try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(zipResponse.body()))) {
                        java.util.zip.ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            String name = entry.getName();
                            int firstSlash = name.indexOf('/');
                            if (firstSlash != -1) {
                                String relPath = name.substring(firstSlash + 1);
                                if (!relPath.isEmpty() && !entry.isDirectory()) {
                                    filePaths.add(relPath);

                                    for (String target : targetKeyFiles) {
                                        if (relPath.equalsIgnoreCase(target) || relPath.endsWith("/" + target)) {
                                            byte[] buffer = zis.readAllBytes();
                                            keyFilesContent.put(target, new String(buffer, java.nio.charset.StandardCharsets.UTF_8));
                                        }
                                    }
                                }
                            }
                            zis.closeEntry();
                        }
                    }

                    if (!filePaths.isEmpty()) {
                        return new ScanResult(owner, repo, branch, filePaths, keyFilesContent);
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public static class ParsedGithubUrl {
        private final String owner;
        private final String repo;
        private final String branch;
        private final String cleanCloneUrl;

        public ParsedGithubUrl(String owner, String repo, String branch, String cleanCloneUrl) {
            this.owner = owner;
            this.repo = repo;
            this.branch = branch;
            this.cleanCloneUrl = cleanCloneUrl;
        }

        public String getOwner() { return owner; }
        public String getRepo() { return repo; }
        public String getBranch() { return branch; }
        public String getCleanCloneUrl() { return cleanCloneUrl; }
    }

    public ParsedGithubUrl parseGithubUrlDetailed(String url) {
        String cleanUrl = url.trim().replace("https://github.com/", "").replace("http://github.com/", "");
        if (cleanUrl.endsWith(".git")) {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 4);
        }
        String[] parts = cleanUrl.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Định dạng GitHub repository URL không hợp lệ: " + url);
        }
        String owner = parts[0];
        String repo = parts[1];
        String branch = null;
        if (parts.length >= 4 && ("tree".equals(parts[2]) || "blob".equals(parts[2]))) {
            branch = parts[3];
        }
        String cloneUrl = String.format("https://github.com/%s/%s.git", owner, repo);
        return new ParsedGithubUrl(owner, repo, branch, cloneUrl);
    }

    public String[] parseGithubUrl(String url) {
        ParsedGithubUrl p = parseGithubUrlDetailed(url);
        return new String[]{p.getOwner(), p.getRepo()};
    }
}
