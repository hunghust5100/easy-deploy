package com.easydeploy.web.service;

import com.easydeploy.core.model.ProjectConfig;
import com.easydeploy.core.ssh.SshDeployCoreService;
import com.easydeploy.web.dto.response.DeploymentResponse;
import com.easydeploy.web.entity.DeploymentEntity;
import com.easydeploy.web.entity.ProjectEntity;
import com.easydeploy.web.entity.ServerEntity;
import com.easydeploy.web.entity.UserEntity;
import com.easydeploy.web.repository.DeploymentRepository;
import com.easydeploy.web.repository.ProjectRepository;
import com.easydeploy.web.repository.ServerRepository;
import com.easydeploy.web.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class DeploymentService {

    private static final Logger log = LoggerFactory.getLogger(DeploymentService.class);
    private final SshDeployCoreService sshDeployCoreService = new SshDeployCoreService();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final DeploymentRepository deploymentRepository;
    private final ProjectRepository projectRepository;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;

    public DeploymentService(DeploymentRepository deploymentRepository,
                             ProjectRepository projectRepository,
                             ServerRepository serverRepository,
                             UserRepository userRepository) {
        this.deploymentRepository = deploymentRepository;
        this.projectRepository = projectRepository;
        this.serverRepository = serverRepository;
        this.userRepository = userRepository;
    }

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
        execute1ClickDeploy(config, creds, wsSession, null, null, null);
    }

    public void execute1ClickDeploy(ProjectConfig config, SshCredentials creds, WebSocketSession wsSession, UUID projectId, UUID serverId) {
        execute1ClickDeploy(config, creds, wsSession, projectId, serverId, null);
    }

    public void execute1ClickDeploy(ProjectConfig config, SshCredentials creds, WebSocketSession wsSession, UUID projectId, UUID serverId, UUID userId) {
        executorService.submit(() -> {
            DeploymentEntity deployment = null;
            StringBuilder logBuffer = new StringBuilder();
            LocalDateTime start = LocalDateTime.now();

            try {
                ProjectEntity project = (projectId != null) ? projectRepository.findById(projectId).orElse(null) : null;
                ServerEntity server = (serverId != null) ? serverRepository.findById(serverId).orElse(null) : null;

                UserEntity user = null;
                if (project != null && project.getUser() != null) {
                    user = project.getUser();
                } else if (server != null && server.getUser() != null) {
                    user = server.getUser();
                } else if (userId != null) {
                    user = userRepository.findById(userId).orElse(null);
                }

                deployment = new DeploymentEntity();
                deployment.setProject(project);
                deployment.setUser(user);
                deployment.setServer(server);
                deployment.setTriggerSource("WEB_UI");
                deployment.setStatus("RUNNING");
                deployment.setStartedAt(start);
                deployment = deploymentRepository.save(deployment);
            } catch (Exception ex) {
                log.warn("Could not create initial deployment record in DB: {}", ex.getMessage());
            }

            final UUID deploymentId = (deployment != null) ? deployment.getId() : null;

            try {
                sshDeployCoreService.deploy(config, creds, null, logLine -> {
                    logBuffer.append(logLine);
                    sendLog(wsSession, logLine);
                });

                if (deploymentId != null) {
                    updateDeploymentStatus(deploymentId, "SUCCESS", logBuffer.toString(), start);
                }
            } catch (Exception e) {
                log.error("Lỗi trong quá trình 1-Click Deploy", e);
                String errText = "\r\n\u001b[31;1m[FATAL ERROR] " + e.getMessage() + "\u001b[0m\r\n";
                logBuffer.append(errText);
                sendLog(wsSession, errText);

                if (deploymentId != null) {
                    updateDeploymentStatus(deploymentId, "FAILED", logBuffer.toString(), start);
                }
            }
        });
    }

    @Transactional
    public void updateDeploymentStatus(UUID deploymentId, String status, String fullLogs, LocalDateTime startedAt) {
        try {
            DeploymentEntity d = deploymentRepository.findById(deploymentId).orElse(null);
            if (d != null) {
                d.setStatus(status);
                d.setLogContent(fullLogs);
                LocalDateTime finish = LocalDateTime.now();
                d.setFinishedAt(finish);
                d.setDurationSeconds((int) Duration.between(startedAt, finish).getSeconds());
                deploymentRepository.save(d);
            }
        } catch (Exception e) {
            log.error("Failed to update deployment record {}: {}", deploymentId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<DeploymentResponse> getDeploymentsByProjectId(UUID projectId) {
        return deploymentRepository.findByProjectIdOrderByStartedAtDesc(projectId).stream()
                .map(e -> DeploymentResponse.fromEntity(e, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeploymentResponse> getDeploymentsByUserId(UUID userId) {
        return deploymentRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(e -> DeploymentResponse.fromEntity(e, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeploymentResponse getDeploymentById(UUID id) {
        DeploymentEntity entity = deploymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deployment record not found with id: " + id));
        return DeploymentResponse.fromEntity(entity, true);
    }

    private void sendLog(WebSocketSession session, String text) {
        if (session != null) {
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
}

