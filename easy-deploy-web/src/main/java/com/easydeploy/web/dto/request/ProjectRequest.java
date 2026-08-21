package com.easydeploy.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProjectRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    private UUID serverId;

    @NotBlank(message = "appName is required")
    private String appName;

    private String repoUrl;
    private String gitBranch = "main";
    private String techStack = "JAVA_MAVEN";
    private String techVersion = "21";
    private int appPort = 8080;
    private int hostPort = 8080;

    private String dbType = "NONE";
    private String dbName = "app_db";
    private String dbUser = "postgres";
    private String dbPass = "secret";
    private int dbPort = 5432;

    private boolean enableNginx = true;
    private String domainName = "localhost";

    private boolean enableCicd = true;
    private String dockerHubUser = "username";
    private String deployPath = "/root/my-app";

    private boolean enableServerSetup = false;
    private boolean installNginx = false;
    private boolean installCertbot = false;
    private boolean setupFirewall = false;
    private boolean installDocker = false;
    private boolean useSslipIo = false;

    private boolean useDockerHub = false;
    private String dockerHubUsername = "";
    private String dockerHubToken = "";
    private String dockerImageTag = "latest";
    private String deployMode = "remote_build";
    private String adminEmail = "";

    private Map<String, String> envVars = new HashMap<>();
    private java.util.List<com.easydeploy.core.model.ServiceModule> services = new java.util.ArrayList<>();

    public ProjectRequest() {}

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getServerId() { return serverId; }
    public void setServerId(UUID serverId) { this.serverId = serverId; }

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

    public String getDbPass() { return dbPass; }
    public void setDbPass(String dbPass) { this.dbPass = dbPass; }

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

    public String getDockerHubToken() { return dockerHubToken; }
    public void setDockerHubToken(String dockerHubToken) { this.dockerHubToken = dockerHubToken; }

    public String getDockerImageTag() { return dockerImageTag; }
    public void setDockerImageTag(String dockerImageTag) { this.dockerImageTag = dockerImageTag; }

    public String getDeployMode() { return deployMode; }
    public void setDeployMode(String deployMode) { this.deployMode = deployMode; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public Map<String, String> getEnvVars() { return envVars; }
    public void setEnvVars(Map<String, String> envVars) { this.envVars = envVars; }

    public java.util.List<com.easydeploy.core.model.ServiceModule> getServices() {
        if (services == null) {
            services = new java.util.ArrayList<>();
        }
        return services;
    }

    public void setServices(java.util.List<com.easydeploy.core.model.ServiceModule> services) {
        this.services = (services != null) ? services : new java.util.ArrayList<>();
    }
}
