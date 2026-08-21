package com.easydeploy.web.dto.response;

import com.easydeploy.web.entity.ServerEntity;
import java.time.LocalDateTime;
import java.util.UUID;

public class ServerResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private String host;
    private int sshPort;
    private String sshUser;
    private String authType;
    private String password;
    private String privateKey;
    private String defaultDeployPath;
    private boolean dockerInstalled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ServerResponse() {}

    public static ServerResponse fromEntity(ServerEntity entity) {
        if (entity == null) return null;
        ServerResponse dto = new ServerResponse();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        dto.setName(entity.getName());
        dto.setHost(entity.getHost());
        dto.setSshPort(entity.getSshPort());
        dto.setSshUser(entity.getSshUser());
        dto.setAuthType(entity.getAuthType());
        dto.setPassword(entity.getPassword());
        dto.setPrivateKey(entity.getPrivateKey());
        dto.setDefaultDeployPath(entity.getDefaultDeployPath());
        dto.setDockerInstalled(entity.isDockerInstalled());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getSshPort() { return sshPort; }
    public void setSshPort(int sshPort) { this.sshPort = sshPort; }

    public String getSshUser() { return sshUser; }
    public void setSshUser(String sshUser) { this.sshUser = sshUser; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

    public String getDefaultDeployPath() { return defaultDeployPath; }
    public void setDefaultDeployPath(String defaultDeployPath) { this.defaultDeployPath = defaultDeployPath; }

    public boolean isDockerInstalled() { return dockerInstalled; }
    public void setDockerInstalled(boolean dockerInstalled) { this.dockerInstalled = dockerInstalled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
