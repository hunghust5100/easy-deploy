package com.easydeploy.cli.command;

import com.easydeploy.cli.ui.AnsiConsoleHelper;
import com.easydeploy.core.generator.FileWriterService;
import com.easydeploy.core.generator.FileWriterService.GenerationSummary;
import com.easydeploy.core.generator.FileWriterService.GeneratedFile;
import com.easydeploy.core.model.ProjectConfig;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

@Command(
    name = "init",
    mixinStandardHelpOptions = true,
    description = "Interactive CLI Wizard to step-by-step configure and generate complete DevOps stack"
)
public class InitCommand implements Runnable {

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        ProjectConfig config = new ProjectConfig();

        AnsiConsoleHelper.printBanner();
        AnsiConsoleHelper.printSection("🧙 Easy-Deploy Interactive Configuration Wizard");

        // 1. App Name
        System.out.print(AnsiConsoleHelper.BOLD + "1. Tên Ứng dụng (App Name) [default: my-app]: " + AnsiConsoleHelper.RESET);
        String appName = scanner.nextLine().trim();
        if (!appName.isEmpty()) config.setAppName(appName.toLowerCase().replaceAll("[^a-z0-9_-]", "-"));

        // 2. Tech Stack
        System.out.println("\n" + AnsiConsoleHelper.BOLD + "2. Chọn Nền tảng Công nghệ (Tech Stack):" + AnsiConsoleHelper.RESET);
        System.out.println("  1) Java Spring Boot (Maven)");
        System.out.println("  2) Java Spring Boot (Gradle)");
        System.out.println("  3) Node.js Frontend (React / Vite / Next.js / Vue)");
        System.out.println("  4) Node.js Backend (Express / NestJS / Fastify)");
        System.out.println("  5) Python (FastAPI / Django / Flask)");
        System.out.println("  6) Go (Golang)");
        System.out.println("  7) Rust");
        System.out.print(AnsiConsoleHelper.CYAN + "Lựa chọn [1-7, default: 1]: " + AnsiConsoleHelper.RESET);
        String stackChoice = scanner.nextLine().trim();

        switch (stackChoice) {
            case "2":
                config.setTechStack("JAVA_GRADLE");
                config.setTechVersion("21");
                config.setAppPort(8080);
                config.setHostPort(8080);
                break;
            case "3":
                config.setTechStack("NODE_FRONTEND");
                config.setTechVersion("20");
                config.setAppPort(3000);
                config.setHostPort(80);
                break;
            case "4":
                config.setTechStack("NODE_BACKEND");
                config.setTechVersion("20");
                config.setAppPort(3000);
                config.setHostPort(3000);
                break;
            case "5":
                config.setTechStack("PYTHON");
                config.setTechVersion("3.11");
                config.setAppPort(8000);
                config.setHostPort(8000);
                break;
            case "6":
                config.setTechStack("GO");
                config.setTechVersion("1.22");
                config.setAppPort(8080);
                config.setHostPort(8080);
                break;
            case "7":
                config.setTechStack("RUST");
                config.setAppPort(8080);
                config.setHostPort(8080);
                break;
            case "1":
            default:
                config.setTechStack("JAVA_MAVEN");
                config.setTechVersion("21");
                config.setAppPort(8080);
                config.setHostPort(8080);
                break;
        }

        // 3. Port Configuration
        System.out.print("\n" + AnsiConsoleHelper.BOLD + "3. Cổng chạy trong Container (App Port) [default: " + config.getAppPort() + "]: " + AnsiConsoleHelper.RESET);
        String portInput = scanner.nextLine().trim();
        if (!portInput.isEmpty()) {
            try { config.setAppPort(Integer.parseInt(portInput)); } catch (Exception ignored) {}
        }

        System.out.print(AnsiConsoleHelper.BOLD + "   Cổng mở trên Máy chủ VPS (Host Port) [default: " + config.getHostPort() + "]: " + AnsiConsoleHelper.RESET);
        String hostPortInput = scanner.nextLine().trim();
        if (!hostPortInput.isEmpty()) {
            try { config.setHostPort(Integer.parseInt(hostPortInput)); } catch (Exception ignored) {}
        }

        // 4. Database Selection
        System.out.println("\n" + AnsiConsoleHelper.BOLD + "4. Chọn Cơ sở Dữ liệu (Database Service):" + AnsiConsoleHelper.RESET);
        System.out.println("  1) PostgreSQL (Port 5432)");
        System.out.println("  2) MySQL 8.0 (Port 3306)");
        System.out.println("  3) MariaDB (Port 3306)");
        System.out.println("  4) MongoDB (Port 27017)");
        System.out.println("  5) Redis (Port 6379)");
        System.out.println("  6) Không sử dụng Database (None)");
        System.out.print(AnsiConsoleHelper.CYAN + "Lựa chọn [1-6, default: 1]: " + AnsiConsoleHelper.RESET);
        String dbChoice = scanner.nextLine().trim();

        switch (dbChoice) {
            case "2":
                config.setDbType("MYSQL");
                config.setDbPort(3306);
                break;
            case "3":
                config.setDbType("MARIADB");
                config.setDbPort(3306);
                break;
            case "4":
                config.setDbType("MONGODB");
                config.setDbPort(27017);
                break;
            case "5":
                config.setDbType("REDIS");
                config.setDbPort(6379);
                break;
            case "6":
                config.setDbType("NONE");
                break;
            case "1":
            default:
                config.setDbType("POSTGRESQL");
                config.setDbPort(5432);
                break;
        }

        // 5. Nginx & Domain
        System.out.print("\n" + AnsiConsoleHelper.BOLD + "5. Bật Nginx Reverse Proxy? (Y/n) [default: Y]: " + AnsiConsoleHelper.RESET);
        String nginxInput = scanner.nextLine().trim();
        if ("n".equalsIgnoreCase(nginxInput)) {
            config.setEnableNginx(false);
        } else {
            config.setEnableNginx(true);
            System.out.print("   Tên miền / Domain (e.g. app.example.com) [default: localhost]: ");
            String domain = scanner.nextLine().trim();
            if (!domain.isEmpty()) config.setDomainName(domain);
        }

        // 6. GitHub Actions CI/CD
        System.out.print("\n" + AnsiConsoleHelper.BOLD + "6. Sinh Workflow GitHub Actions CI/CD? (Y/n) [default: Y]: " + AnsiConsoleHelper.RESET);
        String cicdInput = scanner.nextLine().trim();
        if ("n".equalsIgnoreCase(cicdInput)) {
            config.setEnableCicd(false);
        } else {
            config.setEnableCicd(true);
            System.out.print("   Docker Hub Username [default: mydockeruser]: ");
            String dUser = scanner.nextLine().trim();
            if (!dUser.isEmpty()) {
                config.setDockerHubUser(dUser);
                config.setDockerHubUsername(dUser);
            }
        }

        // 7. Docker Hub & Deployment Strategy
        System.out.println("\n" + AnsiConsoleHelper.BOLD + "7. Chọn Chiến lược Triển khai (Deployment Strategy):" + AnsiConsoleHelper.RESET);
        System.out.println("  1) Remote Build trên VPS (Build Docker image trực tiếp trên VPS)");
        System.out.println("  2) Docker Hub Registry Pull (Đóng gói Image lên Docker Hub, VPS chỉ kéo image về chạy - Tiết kiệm RAM)");
        System.out.print(AnsiConsoleHelper.CYAN + "Lựa chọn [1-2, default: 1]: " + AnsiConsoleHelper.RESET);
        String modeChoice = scanner.nextLine().trim();
        if ("2".equals(modeChoice)) {
            config.setDeployMode("registry_pull");
            config.setUseDockerHub(true);
            if (config.getDockerHubUsername() == null || config.getDockerHubUsername().isEmpty() || config.getDockerHubUsername().equals("mydockeruser")) {
                System.out.print("   Nhập Docker Hub Username: ");
                String dhUser = scanner.nextLine().trim();
                if (!dhUser.isEmpty()) {
                    config.setDockerHubUsername(dhUser);
                    config.setDockerHubUser(dhUser);
                }
            }
            System.out.print("   Nhập Docker Image Tag [default: latest]: ");
            String dhTag = scanner.nextLine().trim();
            if (!dhTag.isEmpty()) config.setDockerImageTag(dhTag);
        } else {
            config.setDeployMode("remote_build");
        }

        // Confirmation Table
        AnsiConsoleHelper.printSection("📋 Tổng kết Cấu hình đã chọn");
        AnsiConsoleHelper.printConfigTable(config);

        System.out.print("\n" + AnsiConsoleHelper.BOLD + "👉 Bạn có muốn sinh toàn bộ file cấu hình vào thư mục hiện tại? (Y/n) [default: Y]: " + AnsiConsoleHelper.RESET);
        String confirm = scanner.nextLine().trim();
        if ("n".equalsIgnoreCase(confirm)) {
            AnsiConsoleHelper.printWarning("Đã hủy quá trình tạo cấu hình.");
            return;
        }

        try {
            AnsiConsoleHelper.printSection("🚀 Đang khởi tạo các tệp tin DevOps");
            Path targetDir = Paths.get(".").toAbsolutePath().normalize();
            FileWriterService writer = new FileWriterService();
            GenerationSummary summary = writer.generateAll(config, targetDir, true, true);

            for (GeneratedFile file : summary.getFiles()) {
                AnsiConsoleHelper.printSuccess("Sinh thành công: " + file.getRelativePath());
            }

            AnsiConsoleHelper.printSection("✨ Hoàn tất");
            System.out.println(AnsiConsoleHelper.GREEN + "Các file đã sẵn sàng! Bạn có thể thử nghiệm:" + AnsiConsoleHelper.RESET);
            System.out.println("   " + AnsiConsoleHelper.BOLD + "docker compose up -d --build" + AnsiConsoleHelper.RESET);
            System.out.println(AnsiConsoleHelper.GREEN + "Hoặc triển khai ngay lên máy chủ VPS:" + AnsiConsoleHelper.RESET);
            System.out.println("   " + AnsiConsoleHelper.BOLD + "easy-deploy deploy --host <vps_ip> --user root" + AnsiConsoleHelper.RESET);
            System.out.println();

        } catch (Exception e) {
            AnsiConsoleHelper.printError("Lỗi khi sinh file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
