package com.easydeploy.web.dto.response;

import com.easydeploy.core.model.ProjectConfig;
import com.easydeploy.core.model.ServiceModule;
import com.easydeploy.web.entity.ProjectEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProjectResponse {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private UUID id;
    private UUID userId;
    private UUID serverId;
    private String serverName;
    private String appName;
    private String repoUrl;
    private String gitBranch;
    private String techStack;
    private String techVersion;
    private int appPort;
    private int hostPort;
    private String dbType;
    private String dbName;
    private String dbUser;
    private int dbPort;
    private boolean enableNginx;
    private String domainName;
    private boolean enableCicd;
    private String dockerHubUser;
    private String deployPath;
    private boolean enableServerSetup;
    private boolean installNginx;
    private boolean installCertbot;
    private boolean setupFirewall;
    private boolean installDocker;
    private boolean useSslipIo;
    private boolean useDockerHub;
    private String dockerHubUsername;
    private String dockerImageTag;
    private String deployMode;
    private String adminEmail;
    private String status;
    private Map<String, String> envVars;
    private List<ServiceModule> services = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectResponse() {}

    public static ProjectResponse fromEntity(ProjectEntity entity) {
        if (entity == null) return null;
        ProjectResponse dto = new ProjectResponse();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        if (entity.getServer() != null) {
            dto.setServerId(entity.getServer().getId());
            dto.setServerName(entity.getServer().getName());
        }
        dto.setAppName(entity.getAppName());
        dto.setRepoUrl(entity.getRepoUrl());
        dto.setGitBranch(entity.getGitBranch());
        dto.setTechStack(entity.getTechStack());
        dto.setTechVersion(entity.getTechVersion());
        dto.setAppPort(entity.getAppPort());
        dto.setHostPort(entity.getHostPort());
        dto.setDbType(entity.getDbType());
        dto.setDbName(entity.getDbName());
        dto.setDbUser(entity.getDbUser());
        dto.setDbPort(entity.getDbPort());
        dto.setEnableNginx(entity.isEnableNginx());
        dto.setDomainName(entity.getDomainName());
        dto.setEnableCicd(entity.isEnableCicd());
        dto.setDockerHubUser(entity.getDockerHubUser());
        dto.setDeployPath(entity.getDeployPath());
        dto.setEnableServerSetup(entity.isEnableServerSetup());
        dto.setInstallNginx(entity.isInstallNginx());
        dto.setInstallCertbot(entity.isInstallCertbot());
        dto.setSetupFirewall(entity.isSetupFirewall());
        dto.setInstallDocker(entity.isInstallDocker());
        dto.setUseSslipIo(entity.isUseSslipIo());
        dto.setUseDockerHub(entity.isUseDockerHub());
        dto.setDockerHubUsername(entity.getDockerHubUsername());
        dto.setDockerImageTag(entity.getDockerImageTag());
        dto.setDeployMode(entity.getDeployMode());
        dto.setAdminEmail(entity.getAdminEmail());
        dto.setStatus(entity.getStatus());
        dto.setEnvVars(entity.getEnvVars());

        if (entity.getServicesJson() != null && !entity.getServicesJson().trim().isEmpty() && !entity.getServicesJson().equals("[]")) {
            try {
                List<ServiceModule> svcs = OBJECT_MAPPER.readValue(entity.getServicesJson(), new TypeReference<List<ServiceModule>>() {});
                dto.setServices(svcs);
            } catch (Exception ignored) {}
        }

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public ProjectConfig toProjectConfig() {
        ProjectConfig config = new ProjectConfig();
        config.setAppName(this.appName);
        config.setRepoUrl(this.repoUrl);
        config.setGitBranch(this.gitBranch);
        config.setTechStack(this.techStack);
        config.setTechVersion(this.techVersion);
        config.setAppPort(this.appPort);
        config.setHostPort(this.hostPort);
        config.setDbType(this.dbType);
        config.setDbName(this.dbName);
        config.setDbUser(this.dbUser);
        config.setDbPort(this.dbPort);
        config.setEnableNginx(this.enableNginx);
        config.setDomainName(this.domainName);
        config.setEnableCicd(this.enableCicd);
        config.setDockerHubUser(this.dockerHubUser);
        config.setDeployPath(this.deployPath);
        config.setEnableServerSetup(this.enableServerSetup);
        config.setInstallNginx(this.installNginx);
        config.setInstallCertbot(this.installCertbot);
        config.setSetupFirewall(this.setupFirewall);
        config.setInstallDocker(this.installDocker);
        config.setUseSslipIo(this.useSslipIo);
        config.setUseDockerHub(this.useDockerHub);
        config.setDockerHubUsername(this.dockerHubUsername);
        config.setDockerImageTag(this.dockerImageTag);
        config.setDeployMode(this.deployMode);
        config.setAdminEmail(this.adminEmail);
        if (this.envVars != null) {
            config.setEnvVars(this.envVars);
        }
        if (this.services != null && !this.services.isEmpty()) {
            config.setServices(this.services);
        }
        return config;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getServerId() { return serverId; }
    public void setServerId(UUID serverId) { this.serverId = serverId; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public String getGitBranch() { return gitBranch; }
    public void setGitBranch(String gitBranch) { this.gitBranch = gitBranch; }

    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }

    public String getTechVersion() { return techVersion; }
    public void setTechVersion(String techVersion) { this.techVersion = techVersion; }

    public int getAppPort() { return appPort; }
    public void setAppPort(int appPort) { this.appPort = appPort; }

    public int getHostPort() { return hostPort; }
    public void setHostPort(int hostPort) { this.hostPort = hostPort; }

    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }

    public String getDbName() { return dbName; }
    public void setDbName(String dbName) { this.dbName = dbName; }

    public String getDbUser() { return dbUser; }
    public void setDbUser(String dbUser) { this.dbUser = dbUser; }

    public int getDbPort() { return dbPort; }
    public void setDbPort(int dbPort) { this.dbPort = dbPort; }

    public boolean isEnableNginx() { return enableNginx; }
    public void setEnableNginx(boolean enableNginx) { this.enableNginx = enableNginx; }

    public String getDomainName() { return domainName; }
    public void setDomainName(String domainName) { this.domainName = domainName; }

    public boolean isEnableCicd() { return enableCicd; }
    public void setEnableCicd(boolean enableCicd) { this.enableCicd = enableCicd; }

    public String getDockerHubUser() { return dockerHubUser; }
    public void setDockerHubUser(String dockerHubUser) { this.dockerHubUser = dockerHubUser; }

    public String getDeployPath() { return deployPath; }
    public void setDeployPath(String deployPath) { this.deployPath = deployPath; }

    public boolean isEnableServerSetup() { return enableServerSetup; }
    public void setEnableServerSetup(boolean enableServerSetup) { this.enableServerSetup = enableServerSetup; }

    public boolean isInstallNginx() { return installNginx; }
    public void setInstallNginx(boolean installNginx) { this.installNginx = installNginx; }

    public boolean isInstallCertbot() { return installCertbot; }
    public void setInstallCertbot(boolean installCertbot) { this.installCertbot = installCertbot; }

    public boolean isSetupFirewall() { return setupFirewall; }
    public void setSetupFirewall(boolean setupFirewall) { this.setupFirewall = setupFirewall; }

    public boolean isInstallDocker() { return installDocker; }
    public void setInstallDocker(boolean installDocker) { this.installDocker = installDocker; }

    public boolean isUseSslipIo() { return useSslipIo; }
    public void setUseSslipIo(boolean useSslipIo) { this.useSslipIo = useSslipIo; }

    public boolean isUseDockerHub() { return useDockerHub; }
    public void setUseDockerHub(boolean useDockerHub) { this.useDockerHub = useDockerHub; }

    public String getDockerHubUsername() { return dockerHubUsername; }
    public void setDockerHubUsername(String dockerHubUsername) { this.dockerHubUsername = dockerHubUsername; }

    public String getDockerImageTag() { return dockerImageTag; }
    public void setDockerImageTag(String dockerImageTag) { this.dockerImageTag = dockerImageTag; }

    public String getDeployMode() { return deployMode; }
    public void setDeployMode(String deployMode) { this.deployMode = deployMode; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, String> getEnvVars() { return envVars; }
    public void setEnvVars(Map<String, String> envVars) { this.envVars = envVars; }

    public List<ServiceModule> getServices() {
        if (services == null) {
            services = new ArrayList<>();
        }
        return services;
    }

    public void setServices(List<ServiceModule> services) {
        this.services = (services != null) ? services : new ArrayList<>();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
