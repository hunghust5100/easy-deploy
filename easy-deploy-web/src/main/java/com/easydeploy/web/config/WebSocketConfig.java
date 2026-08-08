package com.easydeploy.web.config;

import com.easydeploy.web.websocket.DeployWebSocketHandler;
import com.easydeploy.web.websocket.SshWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SshWebSocketHandler sshWebSocketHandler;
    private final DeployWebSocketHandler deployWebSocketHandler;

    public WebSocketConfig(SshWebSocketHandler sshWebSocketHandler, DeployWebSocketHandler deployWebSocketHandler) {
        this.sshWebSocketHandler = sshWebSocketHandler;
        this.deployWebSocketHandler = deployWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sshWebSocketHandler, "/ws/ssh")
                .setAllowedOrigins("*");

        registry.addHandler(deployWebSocketHandler, "/ws/deploy-logs")
                .setAllowedOrigins("*");
    }
}
