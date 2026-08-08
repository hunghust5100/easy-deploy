package com.easydeploy.core.model;

import java.util.HashMap;
import java.util.Map;

public class ProjectConfig {
    private String appName = "my-app";
    private String techStack = "JAVA_MAVEN"; // JAVA_MAVEN, JAVA_GRADLE, NODE_FRONTEND, NODE_BACKEND, PYTHON
    private String techVersion = "21";
    private int appPort = 8080;
    private int hostPort = 8080;
    
    private String dbType = "POSTGRESQL"; // POSTGRESQL, MYSQL, MARIADB, MONGODB, REDIS, NONE
    private String dbName = "app_db";
    private String dbUser = "postgres";
    private String dbPass = "secret";
    private int dbPort = 5432;
    
    private boolean enableNginx = true;
    private String domainName = "localhost";
    
    private boolean enableCicd = true;
    private String dockerHubUser = "username";
    private String gitBranch = "main";
    private String deployPath = "/root/my-app";
    
    // Tùy chọn Setup Server & Docker Hub Deployment Mode
    private boolean enableServerSetup = false;
    private boolean installNginx = false;
    private boolean installCertbot = false;
    private boolean setupFirewall = false;
    private boolean installDocker = false;
    private boolean useSslipIo = false;
    
    private boolean useDockerHub = false;
    private String dockerHubUsername = "";
    private String dockerHubToken = "";
    private String dockerImageTag = "";
    private String deployMode = "remote_build"; // "remote_build" hoặc "registry_pull"
    private String adminEmail = "";

    private Map<String, String> envVars = new HashMap<>();

    public ProjectConfig() {}

    // Getters and Setters
    public String getAppName() { return (appName != null && !appName.trim().isEmpty()) ? appName : "my-app"; }
    public void setAppName(String appName) { this.appName = appName; }

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

    public String getGitBranch() { return gitBranch; }
    public void setGitBranch(String gitBranch) { this.gitBranch = gitBranch; }

    public String getDeployPath() { return (deployPath != null && !deployPath.trim().isEmpty()) ? deployPath : "/root/" + getAppName(); }
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
}
