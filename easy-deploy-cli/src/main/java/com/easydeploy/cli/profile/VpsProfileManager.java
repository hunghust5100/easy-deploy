package com.easydeploy.cli.profile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VpsProfileManager {

    public static class VpsProfile {
        private String name = "default";
        private String host = "";
        private int port = 22;
        private String username = "root";
        private String password = "";
        private String keyFilePath = "";
        private String deployPath = "";

        public VpsProfile() {}

        public VpsProfile(String name, String host, int port, String username, String password, String keyFilePath, String deployPath) {
            this.name = name;
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.keyFilePath = keyFilePath;
            this.deployPath = deployPath;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getKeyFilePath() { return keyFilePath; }
        public void setKeyFilePath(String keyFilePath) { this.keyFilePath = keyFilePath; }

        public String getDeployPath() { return deployPath; }
        public void setDeployPath(String deployPath) { this.deployPath = deployPath; }
    }

    private final ObjectMapper mapper;
    private final Path configPath;

    public VpsProfileManager() {
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String userHome = System.getProperty("user.home");
        this.configPath = Paths.get(userHome, ".easy-deploy", "profiles.json");
    }

    public Map<String, VpsProfile> loadAllProfiles() {
        if (!Files.exists(configPath)) {
            return new HashMap<>();
        }
        try {
            return mapper.readValue(configPath.toFile(), new TypeReference<Map<String, VpsProfile>>() {});
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    public Optional<VpsProfile> getProfile(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        return Optional.ofNullable(loadAllProfiles().get(name.trim()));
    }

    public void saveProfile(VpsProfile profile) throws IOException {
        if (profile == null || profile.getName() == null) return;
        Map<String, VpsProfile> profiles = loadAllProfiles();
        profiles.put(profile.getName().trim(), profile);

        if (!Files.exists(configPath.getParent())) {
            Files.createDirectories(configPath.getParent());
        }
        mapper.writeValue(configPath.toFile(), profiles);
    }

    public boolean deleteProfile(String name) throws IOException {
        Map<String, VpsProfile> profiles = loadAllProfiles();
        if (profiles.remove(name) != null) {
            mapper.writeValue(configPath.toFile(), profiles);
            return true;
        }
        return false;
    }
}
