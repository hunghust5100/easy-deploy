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
    
    private Map<String, String> envVars = new HashMap<>();

    public ProjectConfig() {}

    // Getters and Setters
    public String getAppName() { return appName; }
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

    public Map<String, String> getEnvVars() { return envVars; }
    public void setEnvVars(Map<String, String> envVars) { this.envVars = envVars; }
}
