package com.easydeploy.web.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SshWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SshWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, SshClientContext> sshContexts = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private static class SshClientContext {
        Session jschSession;
        ChannelShell channel;
        InputStream in;
        OutputStream out;
        volatile boolean connected = false;

        void close() {
            connected = false;
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (jschSession != null && jschSession.isConnected()) {
                jschSession.disconnect();
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        SshClientContext ctx = sshContexts.get(session.getId());

        // 1. Kiểm tra xem đã kết nối SSH chưa. Nếu chưa, message đầu tiên phải là JSON thông tin SSH
        if (ctx == null || !ctx.connected) {
            try {
                JsonNode json = objectMapper.readTree(payload);
                if (json.has("host") && json.has("username")) {
                    connectSsh(session, json);
                } else {
                    session.sendMessage(new TextMessage("\r\n\u001b[31m[Error] Payload kết nối thiếu host hoặc username\u001b[0m\r\n"));
                }
            } catch (Exception e) {
                log.error("Lỗi parse thông tin SSH từ client", e);
                session.sendMessage(new TextMessage("\r\n\u001b[31m[Error] " + e.getMessage() + "\u001b[0m\r\n"));
            }
            return;
        }

        // 2. Nếu đã kết nối, đẩy phím/lệnh nhập trực tiếp vào SSH OutputStream
        if (ctx.out != null) {
            ctx.out.write(payload.getBytes(StandardCharsets.UTF_8));
            ctx.out.flush();
        }
    }

    private void connectSsh(WebSocketSession session, JsonNode json) throws Exception {
        String host = json.get("host").asText();
        int port = json.has("port") ? json.get("port").asInt(22) : 22;
        String username = json.get("username").asText();
        String password = json.has("password") ? json.get("password").asText("") : "";

        session.sendMessage(new TextMessage("\r\n\u001b[33m[SSH] Đang kết nối tới " + username + "@" + host + ":" + port + "...\u001b[0m\r\n"));

        JSch jsch = new JSch();
        Session jschSession = jsch.getSession(username, host, port);
        if (!password.isEmpty()) {
            jschSession.setPassword(password);
        }
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.connect(10000); // 10s timeout

        ChannelShell channel = (ChannelShell) jschSession.openChannel("shell");
        channel.setPtyType("xterm");

        InputStream in = channel.getInputStream();
        OutputStream out = channel.getOutputStream();

        channel.connect(5000);

        SshClientContext ctx = new SshClientContext();
        ctx.jschSession = jschSession;
        ctx.channel = channel;
        ctx.in = in;
        ctx.out = out;
        ctx.connected = true;

        sshContexts.put(session.getId(), ctx);
        session.sendMessage(new TextMessage("\u001b[32m[SSH] Kết nối thành công!\u001b[0m\r\n\r\n"));

        // 3. Đọc dữ liệu từ SSH InputStream trong Thread riêng và gửi về WebSocket client
        executorService.submit(() -> {
            byte[] buffer = new byte[1024];
            int i;
            try {
                while (ctx.connected && session.isOpen() && (i = in.read(buffer)) != -1) {
                    String output = new String(buffer, 0, i, StandardCharsets.UTF_8);
                    synchronized (session) {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(output));
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Luồng đọc SSH kết thúc: {}", e.getMessage());
            } finally {
                cleanup(session.getId());
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        cleanup(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Transport error trên WebSocket session: {}", session.getId(), exception);
        cleanup(session.getId());
    }

    private void cleanup(String sessionId) {
        SshClientContext ctx = sshContexts.remove(sessionId);
        if (ctx != null) {
            ctx.close();
            log.info("Đã đóng sạch sẽ SSH Session cho WebSocket ID: {}", sessionId);
        }
    }
}
