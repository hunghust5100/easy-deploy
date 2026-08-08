package com.easydeploy.web.service;

import com.easydeploy.core.generator.FileWriterService;
import com.easydeploy.core.model.ProjectConfig;
import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DeploymentService {

    private static final Logger log = LoggerFactory.getLogger(DeploymentService.class);
    private final FileWriterService fileWriterService = new FileWriterService();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public static class SshCredentials {
        private String host;
        private int port = 22;
        private String username;
        private String password;
        private String deployPath; // Thư mục chứa app trên VPS, ví dụ: /root/my-app

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getDeployPath() { return deployPath != null ? deployPath : "/root/" + username; }
        public void setDeployPath(String deployPath) { this.deployPath = deployPath; }
    }

    /**
     * Thực thi quy trình 1-Click Deploy tới VPS từ xa via SSH/SFTP & Stream Logs về WebSocket
     */
    public void execute1ClickDeploy(ProjectConfig config, SshCredentials creds, WebSocketSession wsSession) {
        executorService.submit(() -> {
            Session jschSession = null;
            ChannelExec execChannel = null;

            try {
                sendLog(wsSession, "\u001b[34;1m[1-CLICK DEPLOY] Bắt đầu khởi tạo quy trình triển khai tự động...\u001b[0m\r\n");

                // Bước 1: Tạo file cấu hình tạm thời trên Local
                Path tempDir = Files.createTempDirectory("easy-deploy-");
                sendLog(wsSession, "\u001b[36m[Step 1/4] Sinh các file cấu hình (Dockerfile, docker-compose, nginx, .dockerignore)...\u001b[0m\r\n");
                fileWriterService.generateAll(config, tempDir);

                // Bước 2: Kết nối SSH & SFTP tới VPS
                sendLog(wsSession, "\u001b[36m[Step 2/4] Kết nối SSH tới VPS " + creds.getUsername() + "@" + creds.getHost() + ":" + creds.getPort() + "...\u001b[0m\r\n");
                JSch jsch = new JSch();
                jschSession = jsch.getSession(creds.getUsername(), creds.getHost(), creds.getPort());
                if (creds.getPassword() != null && !creds.getPassword().isEmpty()) {
                    jschSession.setPassword(creds.getPassword());
                }
                jschSession.setConfig("StrictHostKeyChecking", "no");
                jschSession.connect(10000);

                // Tạo thư mục workspace trên VPS qua SFTP
                String targetRemotePath = creds.getDeployPath();
                sendLog(wsSession, "\u001b[36m[Step 3/4] Đang Upload toàn bộ file cấu hình lên VPS tại " + targetRemotePath + "...\u001b[0m\r\n");
                
                ChannelSftp sftp = (ChannelSftp) jschSession.openChannel("sftp");
                sftp.connect(5000);
                mkdirRemoteRecursive(sftp, targetRemotePath);
                uploadDirectory(sftp, tempDir.toFile(), targetRemotePath);
                sftp.disconnect();

                // Bước 3: Thực thi Setup Script & Docker Compose Up trên VPS & Stream Log
                sendLog(wsSession, "\u001b[36m[Step 4/4] Thực thi chuỗi lệnh Triển khai trên VPS...\u001b[0m\r\n\r\n");
                
                StringBuilder commandBuilder = new StringBuilder();
                commandBuilder.append("cd ").append(targetRemotePath);

                // Nếu có file setup-server.sh, chạy setup trước
                File setupScript = tempDir.resolve("setup-server.sh").toFile();
                if (setupScript.exists()) {
                    commandBuilder.append(" && chmod +x setup-server.sh && ./setup-server.sh");
                }

                // Chọn chế độ deploy: registry_pull vs remote_build
                if ("registry_pull".equalsIgnoreCase(config.getDeployMode())) {
                    if (config.isUseDockerHub() && config.getDockerHubUsername() != null && !config.getDockerHubUsername().isEmpty()) {
                        commandBuilder.append(" && echo \"").append(config.getDockerHubToken()).append("\" | docker login -u \"").append(config.getDockerHubUsername()).append("\" --password-stdin");
                    }
                    commandBuilder.append(" && (docker compose pull || docker-compose pull) && (docker compose up -d || docker-compose up -d)");
                } else {
                    commandBuilder.append(" && (docker compose up -d --build 2>&1 || docker-compose up -d --build 2>&1)");
                }

                execChannel = (ChannelExec) jschSession.openChannel("exec");
                execChannel.setCommand(commandBuilder.toString());

                InputStream in = execChannel.getInputStream();
                execChannel.connect(5000);

                // Reading and Streaming Process Output Line by Line
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    sendLog(wsSession, line + "\r\n");
                }

                int exitCode = execChannel.getExitStatus();
                if (exitCode == 0) {
                    sendLog(wsSession, "\r\n\u001b[32;1m[SUCCESS] Triển khai thành công! Ứng dụng của bạn đã sẵn sàng tại port " + config.getHostPort() + "\u001b[0m\r\n");
                } else {
                    sendLog(wsSession, "\r\n\u001b[31;1m[ERROR] Lỗi thực thi quy trình (Exit code: " + exitCode + ")\u001b[0m\r\n");
                }

            } catch (Exception e) {
                log.error("Lỗi trong quá trình 1-Click Deploy", e);
                sendLog(wsSession, "\r\n\u001b[31;1m[FATAL ERROR] " + e.getMessage() + "\u001b[0m\r\n");
            } finally {
                if (execChannel != null && execChannel.isConnected()) execChannel.disconnect();
                if (jschSession != null && jschSession.isConnected()) jschSession.disconnect();
            }
        });
    }

    private void sendLog(WebSocketSession session, String text) {
        synchronized (session) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(text));
                } catch (IOException e) {
                    log.error("Không thể gửi WebSocket log", e);
                }
            }
        }
    }

    private void mkdirRemoteRecursive(ChannelSftp sftp, String remotePath) throws SftpException {
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

    private void uploadDirectory(ChannelSftp sftp, File localFile, String remotePath) throws SftpException, IOException {
        if (localFile.isDirectory()) {
            try { sftp.cd(remotePath); } catch (SftpException e) { sftp.mkdir(remotePath); sftp.cd(remotePath); }
            for (File file : localFile.listFiles()) {
                uploadDirectory(sftp, file, remotePath + "/" + file.getName());
            }
        } else {
            try (FileInputStream fis = new FileInputStream(localFile)) {
                sftp.put(fis, localFile.getName());
            }
        }
    }
}
