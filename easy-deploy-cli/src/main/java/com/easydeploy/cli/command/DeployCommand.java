package com.easydeploy.cli.command;

import com.easydeploy.cli.profile.VpsProfileManager;
import com.easydeploy.cli.profile.VpsProfileManager.VpsProfile;
import com.easydeploy.cli.ui.AnsiConsoleHelper;
import com.easydeploy.core.detector.TechStackRuleEngine;
import com.easydeploy.core.docker.DockerLocalService;
import com.easydeploy.core.generator.FileWriterService;
import com.easydeploy.core.model.ProjectConfig;
import com.easydeploy.core.ssh.SshDeployCoreService;
import com.easydeploy.core.ssh.SshDeployCoreService.SshCredentials;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;

@Command(
    name = "deploy",
    mixinStandardHelpOptions = true,
    description = "1-Click SSH Deploy current project to a remote VPS server (Supports Remote Build or Docker Hub Pull)"
)
public class DeployCommand implements Runnable {

    @Option(names = {"-H", "--host"}, description = "Target VPS Host / IP address (e.g. 103.179.x.x)")
    private String host;

    @Option(names = {"-P", "--port"}, description = "VPS SSH Port (default: 22)", defaultValue = "22")
    private int port;

    @Option(names = {"-u", "--user"}, description = "VPS SSH Username (default: root)", defaultValue = "root")
    private String user;

    @Option(names = {"-p", "--password"}, description = "VPS SSH Password")
    private String password;

    @Option(names = {"-i", "--key-file"}, description = "Path to SSH Private Key (e.g. ~/.ssh/id_rsa)")
    private String keyFile;

    @Option(names = {"-d", "--deploy-path"}, description = "Destination directory on VPS (default: /root/<app_name>)")
    private String deployPath;

    @Option(names = {"-m", "--mode"}, description = "Deploy mode: 'remote_build' (build on VPS) or 'registry_pull' (pull from Docker Hub)", defaultValue = "remote_build")
    private String deployMode;

    @Option(names = {"--push-first"}, description = "Build and push image to Docker Hub locally before deploying to VPS")
    private boolean pushFirst;

    @Option(names = {"--docker-user"}, description = "Docker Hub username")
    private String dockerUser;

    @Option(names = {"--docker-token"}, description = "Docker Hub access token or password for private registry")
    private String dockerToken;

    @Option(names = {"--image-tag"}, description = "Docker image tag (default: latest)", defaultValue = "latest")
    private String imageTag;

    @Option(names = {"--profile"}, description = "Load saved VPS Profile from ~/.easy-deploy/profiles.json")
    private String profileName;

    @Option(names = {"--save-profile"}, description = "Save this VPS connection profile for future use")
    private String saveAsProfile;

    @Option(names = {"--setup-server"}, description = "Execute automated server bootstrap (Docker, Nginx, UFW firewall)", defaultValue = "true")
    private boolean setupServer = true;

    @Option(names = {"--path"}, description = "Local project directory path", defaultValue = ".")
    private String localPath;

    @Option(names = {"-y", "--yes"}, description = "Deploy automatically without confirmation")
    private boolean autoYes;

    @Override
    public void run() {
        AnsiConsoleHelper.printBanner();
        AnsiConsoleHelper.printSection("🚀 1-Click SSH VPS Deployment");

        VpsProfileManager profileManager = new VpsProfileManager();
        Scanner scanner = new Scanner(System.in);

        // 1. Load Profile if specified
        if (profileName != null && !profileName.trim().isEmpty()) {
            Optional<VpsProfile> loaded = profileManager.getProfile(profileName);
            if (loaded.isPresent()) {
                VpsProfile p = loaded.get();
                if (host == null) host = p.getHost();
                if (port == 22 && p.getPort() > 0) port = p.getPort();
                if (user == null || user.equals("root")) user = p.getUsername();
                if (password == null) password = p.getPassword();
                if (keyFile == null) keyFile = p.getKeyFilePath();
                if (deployPath == null) deployPath = p.getDeployPath();
                AnsiConsoleHelper.printSuccess("Loaded VPS Profile: " + profileName + " (" + user + "@" + host + ")");
            } else {
                AnsiConsoleHelper.printWarning("Profile '" + profileName + "' not found. Falling back to parameters or manual prompt.");
            }
        }

        // 2. Interactive Input if host is missing
        if (host == null || host.trim().isEmpty()) {
            System.out.print(AnsiConsoleHelper.BOLD + "VPS Host / IP Address: " + AnsiConsoleHelper.RESET);
            host = scanner.nextLine().trim();

            if (host.isEmpty()) {
                AnsiConsoleHelper.printError("Host cannot be empty!");
                return;
            }

            System.out.print("SSH Port [default: " + port + "]: ");
            String portInput = scanner.nextLine().trim();
            if (!portInput.isEmpty()) {
                try { port = Integer.parseInt(portInput); } catch (Exception ignored) {}
            }

            System.out.print("SSH Username [default: " + user + "]: ");
            String userInput = scanner.nextLine().trim();
            if (!userInput.isEmpty()) user = userInput;

            System.out.print("Authentication Method [1: Password, 2: SSH Key] [default: 1]: ");
            String authMethod = scanner.nextLine().trim();
            if ("2".equals(authMethod)) {
                System.out.print("SSH Key File Path [default: ~/.ssh/id_rsa]: ");
                String keyInput = scanner.nextLine().trim();
                keyFile = keyInput.isEmpty() ? "~/.ssh/id_rsa" : keyInput;

                System.out.print("SSH Key Passphrase (leave empty if none): ");
                password = scanner.nextLine().trim();
            } else {
                System.out.print("SSH Password: ");
                password = scanner.nextLine().trim();
            }
        }

        Path projectDir = Paths.get(localPath).toAbsolutePath().normalize();
        TechStackRuleEngine ruleEngine = new TechStackRuleEngine();
        ProjectConfig config = ruleEngine.analyzeLocalProject(projectDir);

        if (pushFirst) {
            deployMode = "registry_pull";
        }

        config.setDeployMode(deployMode);
        if (dockerUser != null && !dockerUser.trim().isEmpty()) {
            config.setDockerHubUsername(dockerUser.trim());
            config.setUseDockerHub(true);
        }
        if (dockerToken != null && !dockerToken.trim().isEmpty()) {
            config.setDockerHubToken(dockerToken.trim());
        }
        if (imageTag != null && !imageTag.trim().isEmpty()) {
            config.setDockerImageTag(imageTag.trim());
        }

        if (deployPath == null || deployPath.trim().isEmpty()) {
            deployPath = "/root/" + config.getAppName();
        }

        // Handle Push First (Local Build + Push to Docker Hub)
        if (pushFirst || "registry_pull".equalsIgnoreCase(deployMode)) {
            config.setUseDockerHub(true);
            if (config.getDockerHubUsername() == null || config.getDockerHubUsername().isEmpty() || config.getDockerHubUsername().equals("myuser")) {
                System.out.print(AnsiConsoleHelper.BOLD + "Nhập Docker Hub Username: " + AnsiConsoleHelper.RESET);
                String u = scanner.nextLine().trim();
                if (!u.isEmpty()) config.setDockerHubUsername(u);
            }
        }

        if (pushFirst) {
            AnsiConsoleHelper.printSection("📦 Local Docker Build & Push");
            DockerLocalService dockerService = new DockerLocalService();
            if (!dockerService.isDockerInstalled()) {
                AnsiConsoleHelper.printError("Docker daemon không tìm thấy trên máy cục bộ.");
                return;
            }

            // Ensure Dockerfile exists
            if (!Files.exists(projectDir.resolve("Dockerfile"))) {
                try {
                    FileWriterService writer = new FileWriterService();
                    writer.generateAll(config, projectDir, true, false);
                } catch (Exception ignored) {}
            }

            String fullImage = config.getFullDockerImageName();
            boolean built = dockerService.buildImage(projectDir, fullImage, line -> System.out.print(line));
            if (!built) {
                AnsiConsoleHelper.printError("Build image thất bại. Dừng quá trình deploy.");
                return;
            }

            if (config.getDockerHubToken() != null && !config.getDockerHubToken().isEmpty()) {
                dockerService.loginDockerHub(config.getDockerHubUsername(), config.getDockerHubToken(), line -> System.out.print(line));
            }

            boolean pushed = dockerService.pushImage(fullImage, line -> System.out.print(line));
            if (!pushed) {
                AnsiConsoleHelper.printError("Push image lên Docker Hub thất bại. Dừng quá trình deploy.");
                return;
            }
            AnsiConsoleHelper.printSuccess("Đã đẩy image " + fullImage + " lên Docker Hub thành công!");
        }

        // Tự động sinh lại file cấu hình với mode tương ứng nếu cần
        try {
            FileWriterService writer = new FileWriterService();
            writer.generateAll(config, projectDir, true, false);
        } catch (Exception ignored) {}

        // Save profile if requested
        if (saveAsProfile != null && !saveAsProfile.trim().isEmpty()) {
            try {
                VpsProfile p = new VpsProfile(saveAsProfile, host, port, user, password, keyFile, deployPath);
                profileManager.saveProfile(p);
                AnsiConsoleHelper.printSuccess("Saved VPS Profile '" + saveAsProfile + "' successfully!");
            } catch (Exception e) {
                AnsiConsoleHelper.printWarning("Could not save profile: " + e.getMessage());
            }
        }

        // Summary before launch
        System.out.println("\n" + AnsiConsoleHelper.BOLD + "Target VPS  : " + AnsiConsoleHelper.CYAN + user + "@" + host + ":" + port + AnsiConsoleHelper.RESET);
        System.out.println(AnsiConsoleHelper.BOLD + "Deploy Path : " + AnsiConsoleHelper.GREEN + deployPath + AnsiConsoleHelper.RESET);
        System.out.println(AnsiConsoleHelper.BOLD + "Deploy Mode : " + AnsiConsoleHelper.MAGENTA + ("registry_pull".equals(config.getDeployMode()) ? "Docker Hub Pull (" + config.getFullDockerImageName() + ")" : "Remote Build on VPS") + AnsiConsoleHelper.RESET);
        System.out.println(AnsiConsoleHelper.BOLD + "Local App   : " + AnsiConsoleHelper.YELLOW + config.getAppName() + " (" + config.getTechStack() + ")" + AnsiConsoleHelper.RESET);

        if (!autoYes) {
            System.out.print("\n" + AnsiConsoleHelper.BOLD + "👉 Start deployment now? (Y/n) [default: Y]: " + AnsiConsoleHelper.RESET);
            String confirm = scanner.nextLine().trim();
            if ("n".equalsIgnoreCase(confirm)) {
                AnsiConsoleHelper.printWarning("Deployment cancelled.");
                return;
            }
        }

        // Execute Deployment
        AnsiConsoleHelper.printSection("⚡ Executing Remote Deployment");
        SshCredentials creds = new SshCredentials(host, port, user, password, deployPath);
        creds.setKeyFilePath(keyFile);
        creds.setRunSetupScript(setupServer);

        SshDeployCoreService deployService = new SshDeployCoreService();
        boolean success = deployService.deploy(config, creds, projectDir, line -> System.out.print(line));

        if (success) {
            AnsiConsoleHelper.printSection("🎉 Deployment Completed");
            System.out.println(AnsiConsoleHelper.GREEN + "Ứng dụng của bạn đang chạy tại: " + AnsiConsoleHelper.BOLD + "http://" + host + ":" + config.getHostPort() + AnsiConsoleHelper.RESET);
            System.out.println(AnsiConsoleHelper.CYAN + "Quản lý container từ xa bằng lệnh:" + AnsiConsoleHelper.RESET);
            System.out.println("   ssh " + user + "@" + host + " \"cd " + deployPath + " && docker compose ps\"");
            System.out.println();
        } else {
            AnsiConsoleHelper.printError("Triển khai thất bại. Vui lòng kiểm tra lại log chi tiết ở trên.");
        }
    }
}
