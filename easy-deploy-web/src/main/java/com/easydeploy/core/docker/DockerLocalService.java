package com.easydeploy.core.docker;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DockerLocalService {

    public boolean isDockerInstalled() {
        try {
            Process process = new ProcessBuilder("docker", "--version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean buildImage(Path projectDir, String imageName, Consumer<String> logConsumer) {
        logConsumer.accept("\u001b[36m[Docker Build] Bắt đầu đóng gói Docker Image: " + imageName + "...\u001b[0m\r\n");
        return runLocalProcess(
            new ProcessBuilder("docker", "build", "-t", imageName, "."),
            projectDir.toFile(),
            logConsumer
        );
    }

    public boolean loginDockerHub(String username, String passwordOrToken, Consumer<String> logConsumer) {
        logConsumer.accept("\u001b[36m[Docker Login] Đang đăng nhập Docker Hub (" + username + ")...\u001b[0m\r\n");
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "login", "-u", username, "--password-stdin");
            Process process = pb.start();

            if (passwordOrToken != null && !passwordOrToken.isEmpty()) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(passwordOrToken.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            }

            streamProcessOutput(process, logConsumer);
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logConsumer.accept("\u001b[32m✔ Đăng nhập Docker Hub thành công!\u001b[0m\r\n");
                return true;
            } else {
                logConsumer.accept("\u001b[31m✖ Đăng nhập Docker Hub thất bại (Exit code " + exitCode + ")\u001b[0m\r\n");
                return false;
            }
        } catch (Exception e) {
            logConsumer.accept("\u001b[31m✖ Lỗi khi đăng nhập Docker Hub: " + e.getMessage() + "\u001b[0m\r\n");
            return false;
        }
    }

    public boolean pushImage(String imageName, Consumer<String> logConsumer) {
        logConsumer.accept("\u001b[36m[Docker Push] Đang đẩy Image lên Docker Hub: " + imageName + "...\u001b[0m\r\n");
        return runLocalProcess(
            new ProcessBuilder("docker", "push", imageName),
            null,
            logConsumer
        );
    }

    private boolean runLocalProcess(ProcessBuilder pb, File workingDir, Consumer<String> logConsumer) {
        try {
            if (workingDir != null) {
                pb.directory(workingDir);
            }
            pb.redirectErrorStream(true);
            Process process = pb.start();
            streamProcessOutput(process, logConsumer);

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            logConsumer.accept("\u001b[31m✖ Lỗi thực thi Docker: " + e.getMessage() + "\u001b[0m\r\n");
            return false;
        }
    }

    private void streamProcessOutput(Process process, Consumer<String> logConsumer) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logConsumer.accept(line + "\r\n");
            }
        } catch (Exception ignored) {}
    }
}
