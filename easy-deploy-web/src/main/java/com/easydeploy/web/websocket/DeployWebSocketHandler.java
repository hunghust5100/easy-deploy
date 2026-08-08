package com.easydeploy.web.websocket;

import com.easydeploy.core.model.ProjectConfig;
import com.easydeploy.web.service.DeploymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class DeployWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(DeployWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DeploymentService deploymentService;

    public DeployWebSocketHandler(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(message.getPayload());

            // Parse ProjectConfig & SSH Credentials từ JSON Payload
            ProjectConfig config = objectMapper.treeToValue(root.get("config"), ProjectConfig.class);
            DeploymentService.SshCredentials credentials = objectMapper.treeToValue(root.get("credentials"), DeploymentService.SshCredentials.class);

            if (config == null || credentials == null || credentials.getHost() == null) {
                session.sendMessage(new TextMessage("\u001b[31m[Error] Thiếu thông tin config hoặc vps credentials\u001b[0m\r\n"));
                return;
            }

            // Kích hoạt quy trình 1-Click Deploy trong background
            deploymentService.execute1ClickDeploy(config, credentials, session);

        } catch (Exception e) {
            log.error("Lỗi khi xử lý request 1-Click Deploy", e);
            session.sendMessage(new TextMessage("\u001b[31m[Error] " + e.getMessage() + "\u001b[0m\r\n"));
        }
    }
}
