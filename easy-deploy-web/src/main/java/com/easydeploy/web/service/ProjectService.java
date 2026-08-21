package com.easydeploy.web.service;

import com.easydeploy.web.dto.request.ProjectRequest;
import com.easydeploy.web.dto.response.ProjectResponse;
import com.easydeploy.web.entity.ProjectEntity;
import com.easydeploy.web.entity.ServerEntity;
import com.easydeploy.web.entity.UserEntity;
import com.easydeploy.web.repository.ProjectRepository;
import com.easydeploy.web.repository.ServerRepository;
import com.easydeploy.web.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ServerRepository serverRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, ServerRepository serverRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        UserEntity user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }
        if (user == null) {
            user = userRepository.findAll().stream().findFirst().orElseGet(() -> {
                UserEntity defaultUser = new UserEntity();
                defaultUser.setEmail("developer@easydeploy.io");
                defaultUser.setPassword("123456");
                defaultUser.setFullName("Default Developer");
                defaultUser.setRole("DEVELOPER");
                defaultUser.setStatus("ACTIVE");
                return userRepository.save(defaultUser);
            });
        }

        ServerEntity server = null;
        if (request.getServerId() != null) {
            server = serverRepository.findById(request.getServerId())
                    .orElseThrow(() -> new IllegalArgumentException("Server not found with id: " + request.getServerId()));
        }

        ProjectEntity project = new ProjectEntity();
        project.setUser(user);
        project.setServer(server);
        mapRequestToEntity(request, project);

        ProjectEntity saved = projectRepository.save(project);
        return ProjectResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByUserId(UUID userId) {
        return projectRepository.findByUserId(userId).stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));
        return ProjectResponse.fromEntity(project);
    }

    @Transactional(readOnly = true)
    public ProjectEntity getProjectEntityById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, ProjectRequest request) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));

        if (request.getServerId() != null) {
            ServerEntity server = serverRepository.findById(request.getServerId())
                    .orElseThrow(() -> new IllegalArgumentException("Server not found with id: " + request.getServerId()));
            project.setServer(server);
        }

        mapRequestToEntity(request, project);

        ProjectEntity updated = projectRepository.save(project);
        return ProjectResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteProject(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new IllegalArgumentException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
    }

    private void mapRequestToEntity(ProjectRequest req, ProjectEntity entity) {
        if (req.getAppName() != null) entity.setAppName(req.getAppName());
        if (req.getRepoUrl() != null) entity.setRepoUrl(req.getRepoUrl());
        if (req.getGitBranch() != null) entity.setGitBranch(req.getGitBranch());
        if (req.getTechStack() != null) entity.setTechStack(req.getTechStack());
        if (req.getTechVersion() != null) entity.setTechVersion(req.getTechVersion());
        if (req.getAppPort() > 0) entity.setAppPort(req.getAppPort());
        if (req.getHostPort() > 0) entity.setHostPort(req.getHostPort());
        if (req.getDbType() != null) entity.setDbType(req.getDbType());
        if (req.getDbName() != null) entity.setDbName(req.getDbName());
        if (req.getDbUser() != null) entity.setDbUser(req.getDbUser());
        if (req.getDbPass() != null) entity.setDbPass(req.getDbPass());
        if (req.getDbPort() > 0) entity.setDbPort(req.getDbPort());
        entity.setEnableNginx(req.isEnableNginx());
        if (req.getDomainName() != null) entity.setDomainName(req.getDomainName());
        entity.setEnableCicd(req.isEnableCicd());
        if (req.getDockerHubUser() != null) entity.setDockerHubUser(req.getDockerHubUser());
        if (req.getDeployPath() != null) entity.setDeployPath(req.getDeployPath());
        entity.setEnableServerSetup(req.isEnableServerSetup());
        entity.setInstallNginx(req.isInstallNginx());
        entity.setInstallCertbot(req.isInstallCertbot());
        entity.setSetupFirewall(req.isSetupFirewall());
        entity.setInstallDocker(req.isInstallDocker());
        entity.setUseSslipIo(req.isUseSslipIo());
        entity.setUseDockerHub(req.isUseDockerHub());
        if (req.getDockerHubUsername() != null) entity.setDockerHubUsername(req.getDockerHubUsername());
        if (req.getDockerHubToken() != null) entity.setDockerHubToken(req.getDockerHubToken());
        if (req.getDockerImageTag() != null) entity.setDockerImageTag(req.getDockerImageTag());
        if (req.getDeployMode() != null) entity.setDeployMode(req.getDeployMode());
        if (req.getAdminEmail() != null) entity.setAdminEmail(req.getAdminEmail());
        if (req.getEnvVars() != null) entity.setEnvVars(new java.util.HashMap<>(req.getEnvVars()));
        if (req.getServices() != null && !req.getServices().isEmpty()) {
            try {
                entity.setServicesJson(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req.getServices()));
            } catch (Exception ignored) {}
        }
    }
}
