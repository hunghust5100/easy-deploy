package com.easydeploy.web.dto.response;

import com.easydeploy.web.entity.DeploymentEntity;
import java.time.LocalDateTime;
import java.util.UUID;

public class DeploymentResponse {
    private UUID id;
    private UUID projectId;
    private String appName;
    private UUID userId;
    private String userEmail;
    private UUID serverId;
    private String serverName;
    private String triggerSource;
    private String commitHash;
    private String status;
    private String logContent;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer durationSeconds;

    public DeploymentResponse() {}

    public static DeploymentResponse fromEntity(DeploymentEntity entity, boolean includeLogs) {
        if (entity == null) return null;
        DeploymentResponse dto = new DeploymentResponse();
        dto.setId(entity.getId());
        if (entity.getProject() != null) {
            dto.setProjectId(entity.getProject().getId());
            dto.setAppName(entity.getProject().getAppName());
        }
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserEmail(entity.getUser().getEmail());
        }
        if (entity.getServer() != null) {
            dto.setServerId(entity.getServer().getId());
            dto.setServerName(entity.getServer().getName());
        }
        dto.setTriggerSource(entity.getTriggerSource());
        dto.setCommitHash(entity.getCommitHash());
        dto.setStatus(entity.getStatus());
        if (includeLogs) {
            dto.setLogContent(entity.getLogContent());
        }
        dto.setStartedAt(entity.getStartedAt());
        dto.setFinishedAt(entity.getFinishedAt());
        dto.setDurationSeconds(entity.getDurationSeconds());
        return dto;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public UUID getServerId() { return serverId; }
    public void setServerId(UUID serverId) { this.serverId = serverId; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getTriggerSource() { return triggerSource; }
    public void setTriggerSource(String triggerSource) { this.triggerSource = triggerSource; }

    public String getCommitHash() { return commitHash; }
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLogContent() { return logContent; }
    public void setLogContent(String logContent) { this.logContent = logContent; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
}
