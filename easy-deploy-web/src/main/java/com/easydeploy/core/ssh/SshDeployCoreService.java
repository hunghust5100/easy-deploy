package com.easydeploy.core.ssh;

import com.easydeploy.core.generator.FileWriterService;
import com.easydeploy.core.model.ProjectConfig;
import com.easydeploy.core.scanner.GithubTreeScanner;
import com.jcraft.jsch.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class SshDeployCoreService {

    public static class SshCredentials {
        private String host;
        private int port = 22;
        private String username;
        private String password;
        private String keyFilePath; // e.g. ~/.ssh/id_rsa or ~/.ssh/id_ed25519
        private String deployPath; // /root/my-app
        private boolean runSetupScript = true;
        private boolean cleanServerBeforeDeploy = false;

        public SshCredentials() {}

        public SshCredentials(String host, int port, String username, String password, String deployPath) {
            this.host = host;
            this.port = port > 0 ? port : 22;
            this.username = username;
            this.password = password;
            this.deployPath = deployPath;
        }

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port > 0 ? port : 22; }
        public void setPort(int port) { this.port = port; }

        public String getUsername() { return username != null ? username : "root"; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getKeyFilePath() { return keyFilePath; }
        public void setKeyFilePath(String keyFilePath) { this.keyFilePath = keyFilePath; }

        public String getDeployPath() {
            if (deployPath != null && !deployPath.trim().isEmpty()) return deployPath;
            return "/root/" + getUsername();
        }
        public void setDeployPath(String deployPath) { this.deployPath = deployPath; }

        public boolean isRunSetupScript() { return runSetupScript; }
        public void setRunSetupScript(boolean runSetupScript) { this.runSetupScript = runSetupScript; }

        public boolean isCleanServerBeforeDeploy() { return cleanServerBeforeDeploy; }
        public void setCleanServerBeforeDeploy(boolean cleanServerBeforeDeploy) { this.cleanServerBeforeDeploy = cleanServerBeforeDeploy; }
    }

    private final FileWriterService fileWriterService = new FileWriterService();

    public boolean deploy(ProjectConfig config, SshCredentials creds, Path projectDir, Consumer<String> logConsumer) {
        Session jschSession = null;
        ChannelExec execChannel = null;
        Path tempDir = null;

        try {
            logConsumer.accept("\r\n\u001b[34;1m=================================================================\u001b[0m\r\n");
            logConsumer.accept("\u001b[34;1m🚀 BẮT ĐẦU QUY TRÌNH 1-CLICK DEPLOY TỰ ĐỘNG TỚI VPS\u001b[0m\r\n");
            logConsumer.accept("\u001b[34;1m=================================================================\u001b[0m\r\n\r\n");

            // 1. Chuẩn bị file cấu hình
            logConsumer.accept("\u001b[35;1m[EZ_STEP:1]\u001b[0m \u001b[36;1m[Bước 1/5] ⚙️ Chuẩn bị tệp cấu hình triển khai...\u001b[0m\r\n");
            Path sourceDir;
            if (projectDir != null && Files.exists(projectDir.resolve("docker-compose.yml"))) {
                sourceDir = projectDir;
                logConsumer.accept("  ├─ Sử dụng các file cấu hình tại: " + sourceDir + "\r\n");
            } else {
                tempDir = Files.createTempDirectory("easy-deploy-");
                sourceDir = tempDir;
                logConsumer.accept("  ├─ Tự động sinh file cấu hình tạm thời (Dockerfile, Compose, Nginx, .env, Setup)...\r\n");
                fileWriterService.generateAll(config, sourceDir, true, false);
            }
            logConsumer.accept("  └─ \u001b[32m[OK] Hoàn tất chuẩn bị cấu hình.\u001b[0m\r\n\r\n");

            // 2. Kết nối SSH
            String authMethod = (creds.getKeyFilePath() != null && !creds.getKeyFilePath().isEmpty()) ? "SSH Key" : "Password";
            logConsumer.accept("\u001b[35;1m[EZ_STEP:2]\u001b[0m \u001b[36;1m[Bước 2/5] 🔑 Đang thiết lập kết nối SSH tới VPS...\u001b[0m\r\n");
            logConsumer.accept("  ├─ Máy chủ: " + creds.getUsername() + "@" + creds.getHost() + ":" + creds.getPort() + " (Auth: " + authMethod + ")\r\n");

            JSch jsch = new JSch();

            if (creds.getKeyFilePath() != null && !creds.getKeyFilePath().trim().isEmpty()) {
                String expandedKeyPath = creds.getKeyFilePath().replaceFirst("^~", System.getProperty("user.home"));
                File keyFile = new File(expandedKeyPath);
                if (keyFile.exists()) {
                    if (creds.getPassword() != null && !creds.getPassword().isEmpty()) {
                        jsch.addIdentity(keyFile.getAbsolutePath(), creds.getPassword());
                    } else {
                        jsch.addIdentity(keyFile.getAbsolutePath());
                    }
                } else {
                    logConsumer.accept("  ├─ \u001b[33m[Warning] File SSH Key không tồn tại: " + expandedKeyPath + ", chuyển sang dùng Password.\u001b[0m\r\n");
                }
            }

            jschSession = jsch.getSession(creds.getUsername(), creds.getHost(), creds.getPort());
            if (creds.getPassword() != null && !creds.getPassword().isEmpty()) {
                jschSession.setPassword(creds.getPassword());
            }
            jschSession.setConfig("StrictHostKeyChecking", "no");
            jschSession.connect(15000); // 15s timeout
            logConsumer.accept("  └─ \u001b[32m[OK] Kết nối SSH thành công!\u001b[0m\r\n\r\n");

            // 3. Determine deploy strategy based on whether repo has its own Docker files
            String targetRemotePath = creds.getDeployPath();
            boolean hasRepoUrl = config.getRepoUrl() != null && !config.getRepoUrl().trim().isEmpty();

            // Clean server option: stop & remove old containers, remove conflicting nginx sites and old deploy dirs
            if (creds.isCleanServerBeforeDeploy()) {
                logConsumer.accept("  ├─ \u001b[33m[CLEAN] 🧹 Đang dọn sạch toàn bộ container, cấu hình Nginx cũ và thư mục trên VPS...\u001b[0m\r\n");
                String cleanCmd = "echo '>>> [CLEAN] Dừng container và giải phóng cổng...' ; " +
                        "cd " + targetRemotePath + " 2>/dev/null && (docker compose down --rmi all --volumes --remove-orphans 2>/dev/null || docker-compose down --rmi all --volumes --remove-orphans 2>/dev/null || true) ; " +
                        "docker stop $(docker ps -q) 2>/dev/null || true ; " +
                        "docker rm $(docker ps -aq) 2>/dev/null || true ; " +
                        "rm -f /etc/nginx/sites-enabled/* /etc/nginx/sites-available/* 2>/dev/null || true ; " +
                        "rm -rf " + targetRemotePath + " /root/easy-deploy 2>/dev/null || true ; " +
                        "docker network prune -f 2>/dev/null || true ; " +
                        "echo '[CLEAN] Hoàn tất dọn sạch máy chủ!'";

                ChannelExec cleanChannel = (ChannelExec) jschSession.openChannel("exec");
                cleanChannel.setCommand(cleanCmd);
                InputStream cleanIn = cleanChannel.getInputStream();
                InputStream cleanErr = cleanChannel.getErrStream();
                cleanChannel.connect(15000);
                byte[] cleanBuf = new byte[2048];
                while (true) {
                    while (cleanIn.available() > 0) {
                        int len = cleanIn.read(cleanBuf, 0, cleanBuf.length);
                        if (len < 0) break;
                        logConsumer.accept(new String(cleanBuf, 0, len, StandardCharsets.UTF_8));
                    }
                    while (cleanErr.available() > 0) {
                        int len = cleanErr.read(cleanBuf, 0, cleanBuf.length);
                        if (len < 0) break;
                        logConsumer.accept(new String(cleanBuf, 0, len, StandardCharsets.UTF_8));
                    }
                    if (cleanChannel.isClosed()) {
                        if (cleanIn.available() > 0 || cleanErr.available() > 0) continue;
                        break;
                    }
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
                cleanChannel.disconnect();
                logConsumer.accept("  ├─ \u001b[32m[OK] Dọn sạch máy chủ thành công.\u001b[0m\r\n");
            }

            // Step 3: Tải mã nguồn GitHub & Upload Cấu hình
            logConsumer.accept("\u001b[35;1m[EZ_STEP:3]\u001b[0m \u001b[36;1m[Bước 3/5] 📦 Đồng bộ mã nguồn và tệp môi trường (.env)...\u001b[0m\r\n");
            if (hasRepoUrl && !"registry_pull".equalsIgnoreCase(config.getDeployMode())) {
                String rawUrl = config.getRepoUrl().trim();
                String gitUrl = rawUrl;
                String branch = null;

                if (rawUrl.contains("github.com/")) {
                    try {
                        GithubTreeScanner scanner = new GithubTreeScanner();
                        GithubTreeScanner.ParsedGithubUrl parsed = scanner.parseGithubUrlDetailed(rawUrl);
                        gitUrl = parsed.getCleanCloneUrl();
                        branch = parsed.getBranch();
                    } catch (Exception ignored) {}
                }
                if (branch == null && config.getGitBranch() != null && !config.getGitBranch().trim().isEmpty()) {
                    branch = config.getGitBranch().trim();
                }

                logConsumer.accept("  ├─ Đang kéo repository: " + gitUrl + (branch != null ? " (nhánh: " + branch + ")" : "") + "\r\n");
                StringBuilder cloneCmd = new StringBuilder();
                cloneCmd.append("rm -rf ").append(targetRemotePath).append(" && mkdir -p ").append(targetRemotePath);
                if (branch != null && !branch.isEmpty()) {
                    cloneCmd.append(" && (git clone --depth=1 -b ").append(branch).append(" ").append(gitUrl).append(" ").append(targetRemotePath)
                            .append(" || git clone --depth=1 ").append(gitUrl).append(" ").append(targetRemotePath).append(")");
                } else {
                    cloneCmd.append(" && git clone --depth=1 ").append(gitUrl).append(" ").append(targetRemotePath);
                }

                // Run git clone command first
                ChannelExec cloneChannel = (ChannelExec) jschSession.openChannel("exec");
                cloneChannel.setCommand(cloneCmd.toString());
                InputStream cloneIn = cloneChannel.getInputStream();
                InputStream cloneErr = cloneChannel.getErrStream();
                cloneChannel.connect(15000);
                byte[] cloneBuf = new byte[2048];
                while (true) {
                    while (cloneIn.available() > 0) {
                        int len = cloneIn.read(cloneBuf, 0, cloneBuf.length);
                        if (len < 0) break;
                        logConsumer.accept(new String(cloneBuf, 0, len, StandardCharsets.UTF_8));
                    }
                    while (cloneErr.available() > 0) {
                        int len = cloneErr.read(cloneBuf, 0, cloneBuf.length);
                        if (len < 0) break;
                        logConsumer.accept(new String(cloneBuf, 0, len, StandardCharsets.UTF_8));
                    }
                    if (cloneChannel.isClosed()) {
                        if (cloneIn.available() > 0 || cloneErr.available() > 0) continue;
                        break;
                    }
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
                int cloneExit = cloneChannel.getExitStatus();
                cloneChannel.disconnect();

                if (cloneExit != 0) {
                    logConsumer.accept("  ├─ \u001b[33m[Warning] Git clone kết thúc với mã " + cloneExit + ". Tiếp tục với các tệp cấu hình đã upload.\u001b[0m\r\n");
                } else {
                    logConsumer.accept("  ├─ \u001b[32m[OK] Đã tải mã nguồn vào: " + targetRemotePath + "\u001b[0m\r\n");
                }

                // Upload all generated configuration files (docker-compose.yml, Dockerfile, .env, setup-server.sh, .dockerignore, etc.)
                logConsumer.accept("  ├─ Upload các tệp cấu hình (docker-compose.yml, Dockerfile, .env, setup-server.sh) qua SFTP...\r\n");
                ChannelSftp sftp = (ChannelSftp) jschSession.openChannel("sftp");
                sftp.connect(10000);
                mkdirRemoteRecursive(sftp, targetRemotePath);
                uploadDirectory(sftp, sourceDir.toFile(), targetRemotePath);
                sftp.disconnect();
                logConsumer.accept("  └─ \u001b[32m[OK] Hoàn tất nạp tệp cấu hình triển khai.\u001b[0m\r\n\r\n");
            } else {
                // No repo URL or registry_pull mode: upload all generated files
                logConsumer.accept("  ├─ Upload toàn bộ tệp cấu hình qua SFTP vào: " + targetRemotePath + "...\r\n");
                ChannelSftp sftp = (ChannelSftp) jschSession.openChannel("sftp");
                sftp.connect(10000);
                uploadDirectory(sftp, sourceDir.toFile(), targetRemotePath);
                sftp.disconnect();
                logConsumer.accept("  └─ \u001b[32m[OK] Hoàn tất upload tệp cấu hình.\u001b[0m\r\n\r\n");
            }

            // Step 4 & 5: Kích chạy Setup script & Docker Compose
            logConsumer.accept("\u001b[35;1m[EZ_STEP:4]\u001b[0m \u001b[36;1m[Bước 4/5] 🛠️ Cấu hình hạ tầng máy chủ & Biến môi trường...\u001b[0m\r\n");

            StringBuilder cmd = new StringBuilder();
            cmd.append("cd ").append(targetRemotePath);

            // Auto merge missing environment variables from repo's example files (.envExample, .env.example, .env.sample) into .env
            cmd.append(" && (for env_file in .envExample .env.example .env.sample; do ");
            cmd.append("if [ -f \"$env_file\" ]; then ");
            cmd.append("while IFS= read -r line || [ -n \"$line\" ]; do ");
            cmd.append("case \"$line\" in ");
            cmd.append("'#'*|'') ;; ");
            cmd.append("*=*) ");
            cmd.append("k=\"${line%%=*}\"; ");
            cmd.append("k=\"$(echo \"$k\" | tr -d '[:space:]')\"; ");
            cmd.append("if [ -n \"$k\" ] && ! grep -q \"^${k}=\" .env 2>/dev/null; then ");
            cmd.append("echo \"$line\" >> .env; ");
            cmd.append("fi ;; ");
            cmd.append("esac; ");
            cmd.append("done < \"$env_file\"; ");
            cmd.append("fi; ");
            cmd.append("done || true)");

            if (creds.isRunSetupScript()) {
                cmd.append(" && if [ -f setup-server.sh ]; then chmod +x setup-server.sh && ./setup-server.sh; fi");
            }

            cmd.append(" && echo '\r\n\u001b[35;1m[EZ_STEP:5]\u001b[0m \u001b[36;1m[Bước 5/5] 🚀 Khởi chạy Docker Compose & Build Containers...\u001b[0m\r\n'");

            if ("registry_pull".equalsIgnoreCase(config.getDeployMode())) {
                if (config.isUseDockerHub() && config.getDockerHubUsername() != null && !config.getDockerHubUsername().trim().isEmpty() && config.getDockerHubToken() != null && !config.getDockerHubToken().trim().isEmpty()) {
                    cmd.append(" && (echo \"").append(config.getDockerHubToken().trim()).append("\" | docker login -u \"").append(config.getDockerHubUsername().trim()).append("\" --password-stdin || true)");
                }
                cmd.append(" && (docker compose pull || true) && (docker compose up -d || (command -v docker-compose &>/dev/null && docker-compose up -d))");
            } else {
                cmd.append(" && (docker compose up -d --build || (command -v docker-compose &>/dev/null && docker-compose up -d --build))");
            }

            execChannel = (ChannelExec) jschSession.openChannel("exec");
            execChannel.setCommand(cmd.toString());

            InputStream in = execChannel.getInputStream();
            InputStream err = execChannel.getErrStream();
            execChannel.connect(15000);

            byte[] buffer = new byte[2048];
            while (true) {
                while (in.available() > 0) {
                    int len = in.read(buffer, 0, buffer.length);
                    if (len < 0) break;
                    logConsumer.accept(new String(buffer, 0, len, StandardCharsets.UTF_8));
                }
                while (err.available() > 0) {
                    int len = err.read(buffer, 0, buffer.length);
                    if (len < 0) break;
                    logConsumer.accept(new String(buffer, 0, len, StandardCharsets.UTF_8));
                }
                if (execChannel.isClosed()) {
                    if (in.available() > 0 || err.available() > 0) continue;
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }

            int exitCode = execChannel.getExitStatus();
            if (exitCode == 0) {
                String hostIp = creds.getHost();
                int port = config.getHostPort() > 0 ? config.getHostPort() : config.getAppPort();
                String directUrl = "http://" + hostIp + (port == 80 ? "" : ":" + port);

                logConsumer.accept("\r\n\u001b[32;1m=================================================================\u001b[0m\r\n");
                logConsumer.accept("\u001b[32;1m[EZ_STATUS:SUCCESS] 🎉 TRIỂN KHAI 1-CLICK DEPLOY HOÀN TẤT THÀNH CÔNG!\u001b[0m\r\n");
                logConsumer.accept("\u001b[32;1m=================================================================\u001b[0m\r\n");
                logConsumer.accept("\u001b[36m👉 URL Truy cập Trực tiếp: \u001b[32;1m" + directUrl + "\u001b[0m\r\n");
                logConsumer.accept("\u001b[36m👉 Endpoint Kiểm tra:      \u001b[32;1m" + directUrl + "/\u001b[0m\r\n");

                if (config.isEnableNginx() && config.getDomainName() != null && !config.getDomainName().equals("localhost")) {
                    logConsumer.accept("\u001b[36m👉 Domain Nginx:          \u001b[32;1mhttp://" + config.getDomainName() + "\u001b[0m\r\n");
                }
                if (config.isUseSslipIo()) {
                    logConsumer.accept("\u001b[36m👉 HTTPS Auto-Domain:     \u001b[32;1mhttps://" + hostIp + ".sslip.io\u001b[0m\r\n");
                }
                logConsumer.accept("\u001b[32;1m=================================================================\u001b[0m\r\n\r\n");
                return true;
            } else {
                logConsumer.accept("\r\n\u001b[31;1m[EZ_STATUS:ERROR] Triển khai kết thúc thất bại với Exit Code: " + exitCode + "\u001b[0m\r\n");
                return false;
            }

        } catch (Exception e) {
            logConsumer.accept("\r\n\u001b[31;1m[EZ_STATUS:ERROR] [FATAL ERROR] " + e.getMessage() + "\u001b[0m\r\n");
            return false;
        } finally {
            if (execChannel != null && execChannel.isConnected()) execChannel.disconnect();
            if (jschSession != null && jschSession.isConnected()) jschSession.disconnect();
            if (tempDir != null) {
                try {
                    deleteDirectoryRecursively(tempDir);
                } catch (Exception ignored) {}
            }
        }
    }

    private void mkdirRemoteRecursive(ChannelSftp sftp, String remotePath) throws SftpException {
        sftp.cd("/");
        String[] folders = remotePath.split("/");
        for (String folder : folders) {
            if (folder.isEmpty()) continue;
            try {
                sftp.cd(folder);
            } catch (SftpException e) {
                sftp.mkdir(folder);
                sftp.cd(folder);
            }
        }
    }

    private void uploadDirectory(ChannelSftp sftp, File localDir, String remotePath) throws SftpException, IOException {
        mkdirRemoteRecursive(sftp, remotePath);
        File[] files = localDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    uploadDirectory(sftp, file, remotePath + "/" + file.getName());
                } else {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        sftp.cd(remotePath);
                        sftp.put(fis, file.getName());
                    }
                }
            }
        }
    }

    private void deleteDirectoryRecursively(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (var stream = Files.walk(dir)) {
                stream.sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> {
                            try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                        });
            }
        }
    }
}
