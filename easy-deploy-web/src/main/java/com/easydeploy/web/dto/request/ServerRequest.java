package com.easydeploy.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ServerRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    @NotBlank(message = "Server name is required")
    private String name;

    @NotBlank(message = "Host / IP is required")
    private String host;

    private int sshPort = 22;
    private String sshUser = "root";
    private String authType = "PASSWORD";
    private String password;
    private String privateKey;
    private String defaultDeployPath = "/root";
    private boolean dockerInstalled = false;

    public ServerRequest() {}

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
}
