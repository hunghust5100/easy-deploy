package com.easydeploy.web.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private ServerEntity server;

    @Column(nullable = false, length = 100)
    private String appName = "my-app";

    @Column(length = 500)
    private String repoUrl = "";

    @Column(length = 100)
    private String gitBranch = "main";

    @Column(nullable = false, length = 50)
    private String techStack = "JAVA_MAVEN";

    @Column(length = 20)
    private String techVersion = "21";

    @Column(nullable = false)
    private int appPort = 8080;

    @Column(nullable = false)
    private int hostPort = 8080;

    // Database fields
    @Column(length = 30)
    private String dbType = "NONE";

    @Column(length = 100)
    private String dbName = "app_db";

    @Column(length = 50)
    private String dbUser = "postgres";

    @Column(columnDefinition = "TEXT")
    private String dbPass = "secret";

    private int dbPort = 5432;

    // Nginx
    private boolean enableNginx = true;

    @Column(length = 255)
    private String domainName = "localhost";

    // CI/CD
    private boolean enableCicd = true;

    @Column(length = 100)
    private String dockerHubUser = "username";

    @Column(length = 255)
    private String deployPath = "/root/my-app";

    // Advanced & Docker Hub
    private boolean enableServerSetup = false;
    private boolean installNginx = false;
    private boolean installCertbot = false;
    private boolean setupFirewall = false;
    private boolean installDocker = false;
    private boolean useSslipIo = false;

    private boolean useDockerHub = false;

    @Column(length = 100)
    private String dockerHubUsername = "";

    @Column(columnDefinition = "TEXT")
    private String dockerHubToken = "";

    @Column(length = 50)
    private String dockerImageTag = "latest";

    @Column(length = 30)
    private String deployMode = "remote_build";

    @Column(length = 150)
    private String adminEmail = "";

    @Column(length = 30)
    private String status = "READY"; // READY, DEPLOYING, RUNNING, FAILED

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_env_vars", joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyColumn(name = "env_key")
    @Column(name = "env_value", columnDefinition = "TEXT")
    private Map<String, String> envVars = new HashMap<>();

    @Column(columnDefinition = "TEXT")
    private String servicesJson = "[]";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ProjectEntity() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public ServerEntity getServer() { return server; }
    public void setServer(ServerEntity server) { this.server = server; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, String> getEnvVars() { return envVars; }
    public void setEnvVars(Map<String, String> envVars) {
        this.envVars = (envVars != null) ? new HashMap<>(envVars) : new HashMap<>();
    }

    public String getServicesJson() { return servicesJson != null ? servicesJson : "[]"; }
    public void setServicesJson(String servicesJson) { this.servicesJson = servicesJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
