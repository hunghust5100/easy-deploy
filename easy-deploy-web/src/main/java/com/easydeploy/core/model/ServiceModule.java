package com.easydeploy.core.model;

import com.easydeploy.core.model.enums.ServiceType;
import com.easydeploy.core.model.enums.TechStack;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceModule {

    private String id;
    private String name;
    private String relativePath = ".";
    private ServiceType serviceType = ServiceType.BACKEND;
    private String techStack = TechStack.JAVA_MAVEN.name();
    private String techVersion = "21";
    private int containerPort = 8080;
    private int hostPort = 8080;
    private String buildCommand = "";
    private String dockerfile = "Dockerfile";
    private Map<String, String> envVars = new HashMap<>();
    private boolean enabled = true;

    public ServiceModule() {}

    public ServiceModule(String id, String name, String relativePath, ServiceType serviceType, String techStack, String techVersion, int containerPort, int hostPort) {
        this.id = id;
        this.name = name;
        this.relativePath = (relativePath != null && !relativePath.trim().isEmpty()) ? relativePath : ".";
        this.serviceType = serviceType != null ? serviceType : ServiceType.BACKEND;
        this.techStack = techStack != null ? techStack : TechStack.JAVA_MAVEN.name();
        this.techVersion = techVersion != null ? techVersion : "21";
        this.containerPort = containerPort;
        this.hostPort = hostPort;
        this.enabled = true;
    }

    public String getId() {
        return id != null ? id : (name != null ? name : "service");
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return (name != null && !name.trim().isEmpty()) ? name : (id != null ? id : "service");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelativePath() {
        return (relativePath != null && !relativePath.trim().isEmpty()) ? relativePath : ".";
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public ServiceType getServiceType() {
        return serviceType != null ? serviceType : ServiceType.BACKEND;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getTechStack() {
        return techStack != null ? techStack : TechStack.JAVA_MAVEN.name();
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }

    public String getTechVersion() {
        return techVersion != null ? techVersion : "21";
    }

    public void setTechVersion(String techVersion) {
        this.techVersion = techVersion;
    }

    public int getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(int containerPort) {
        this.containerPort = containerPort;
    }

    public int getHostPort() {
        return hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    public String getBuildCommand() {
        return buildCommand != null ? buildCommand : "";
    }

    public void setBuildCommand(String buildCommand) {
        this.buildCommand = buildCommand;
    }

    public String getDockerfile() {
        return dockerfile != null ? dockerfile : "Dockerfile";
    }

    public void setDockerfile(String dockerfile) {
        this.dockerfile = dockerfile;
    }

    public Map<String, String> getEnvVars() {
        if (envVars == null) {
            envVars = new HashMap<>();
        }
        return envVars;
    }

    public void setEnvVars(Map<String, String> envVars) {
        this.envVars = envVars;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFrontend() {
        return serviceType == ServiceType.FRONTEND;
    }

    public boolean isBackend() {
        return serviceType == ServiceType.BACKEND || serviceType == ServiceType.FULLSTACK;
    }
}
