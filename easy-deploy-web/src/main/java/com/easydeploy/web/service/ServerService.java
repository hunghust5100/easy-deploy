package com.easydeploy.web.service;

import com.easydeploy.core.ssh.SshDeployCoreService;
import com.easydeploy.web.dto.request.ServerRequest;
import com.easydeploy.web.dto.response.ServerResponse;
import com.easydeploy.web.entity.ServerEntity;
import com.easydeploy.web.entity.UserEntity;
import com.easydeploy.web.repository.ServerRepository;
import com.easydeploy.web.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServerService {

    private final ServerRepository serverRepository;
    private final UserRepository userRepository;

    public ServerService(ServerRepository serverRepository, UserRepository userRepository) {
        this.serverRepository = serverRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ServerResponse createServer(ServerRequest request) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getUserId()));

        ServerEntity server = new ServerEntity();
        server.setUser(user);
        server.setName(request.getName());
        server.setHost(request.getHost().trim());
        server.setSshPort(request.getSshPort() > 0 ? request.getSshPort() : 22);
        server.setSshUser(request.getSshUser() != null ? request.getSshUser().trim() : "root");
        server.setAuthType(request.getAuthType() != null ? request.getAuthType() : "PASSWORD");
        server.setPassword(request.getPassword());
        server.setPrivateKey(request.getPrivateKey());
        server.setDefaultDeployPath(request.getDefaultDeployPath() != null ? request.getDefaultDeployPath() : "/root");
        server.setDockerInstalled(request.isDockerInstalled());

        ServerEntity saved = serverRepository.save(server);
        return ServerResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ServerResponse> getServersByUserId(UUID userId) {
        return serverRepository.findByUserId(userId).stream()
                .map(ServerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServerResponse getServerById(UUID id) {
        ServerEntity server = serverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Server not found with id: " + id));
        return ServerResponse.fromEntity(server);
    }

    @Transactional(readOnly = true)
    public ServerEntity getServerEntityById(UUID id) {
        return serverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Server not found with id: " + id));
    }

    @Transactional
    public ServerResponse updateServer(UUID id, ServerRequest request) {
        ServerEntity server = serverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Server not found with id: " + id));

        if (request.getName() != null) server.setName(request.getName());
        if (request.getHost() != null) server.setHost(request.getHost().trim());
        if (request.getSshPort() > 0) server.setSshPort(request.getSshPort());
        if (request.getSshUser() != null) server.setSshUser(request.getSshUser().trim());
        if (request.getAuthType() != null) server.setAuthType(request.getAuthType());
        if (request.getPassword() != null) server.setPassword(request.getPassword());
        if (request.getPrivateKey() != null) server.setPrivateKey(request.getPrivateKey());
        if (request.getDefaultDeployPath() != null) server.setDefaultDeployPath(request.getDefaultDeployPath());
        server.setDockerInstalled(request.isDockerInstalled());

        ServerEntity updated = serverRepository.save(server);
        return ServerResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteServer(UUID id) {
        if (!serverRepository.existsById(id)) {
            throw new IllegalArgumentException("Server not found with id: " + id);
        }
        serverRepository.deleteById(id);
    }

    public boolean testConnection(UUID id) {
        ServerEntity server = getServerEntityById(id);
        com.jcraft.jsch.JSch jsch = new com.jcraft.jsch.JSch();
        com.jcraft.jsch.Session session = null;
        try {
            if (server.getPrivateKey() != null && !server.getPrivateKey().trim().isEmpty()) {
                byte[] keyBytes = server.getPrivateKey().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                jsch.addIdentity("server-key", keyBytes, null, (server.getPassword() != null ? server.getPassword().getBytes() : null));
            }
            session = jsch.getSession(server.getSshUser(), server.getHost(), server.getSshPort());
            if (server.getPassword() != null && !server.getPassword().isEmpty()) {
                session.setPassword(server.getPassword());
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(7000);
            return session.isConnected();
        } catch (Exception e) {
            return false;
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
