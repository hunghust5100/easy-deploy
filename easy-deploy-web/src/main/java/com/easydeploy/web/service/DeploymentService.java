package com.easydeploy.web.service;

import com.easydeploy.core.model.ProjectConfig;
import com.easydeploy.core.ssh.SshDeployCoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DeploymentService {

    private static final Logger log = LoggerFactory.getLogger(DeploymentService.class);
    private final SshDeployCoreService sshDeployCoreService = new SshDeployCoreService();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public static class SshCredentials extends SshDeployCoreService.SshCredentials {
        public SshCredentials() {
            super();
        }

        public SshCredentials(String host, int port, String username, String password, String deployPath) {
            super(host, port, username, password, deployPath);
        }
    }

    /**
     * Thực thi quy trình 1-Click Deploy tới VPS từ xa via SSH/SFTP & Stream Logs về WebSocket
     */
    public void execute1ClickDeploy(ProjectConfig config, SshCredentials creds, WebSocketSession wsSession) {
        executorService.submit(() -> {
            try {
                sshDeployCoreService.deploy(config, creds, null, logLine -> sendLog(wsSession, logLine));
            } catch (Exception e) {
                log.error("Lỗi trong quá trình 1-Click Deploy", e);
                sendLog(wsSession, "\r\n\u001b[31;1m[FATAL ERROR] " + e.getMessage() + "\u001b[0m\r\n");
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
}
