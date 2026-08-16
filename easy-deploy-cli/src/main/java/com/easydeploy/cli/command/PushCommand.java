package com.easydeploy.cli.command;

import com.easydeploy.cli.ui.AnsiConsoleHelper;
import com.easydeploy.core.detector.TechStackRuleEngine;
import com.easydeploy.core.docker.DockerLocalService;
import com.easydeploy.core.model.ProjectConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

@Command(
    name = "push",
    mixinStandardHelpOptions = true,
    description = "Push a local Docker image to Docker Hub registry"
)
public class PushCommand implements Runnable {

    @Option(names = {"-t", "--tag"}, description = "Docker image tag to push (e.g. myuser/myapp:latest)")
    private String customTag;

    @Option(names = {"-u", "--user"}, description = "Docker Hub username")
    private String dockerUser;

    @Option(names = {"-p", "--password"}, description = "Docker Hub password or Personal Access Token")
    private String dockerToken;

    @Option(names = {"--path"}, description = "Project directory path", defaultValue = ".")
    private String projectPath;

    @Override
    public void run() {
        AnsiConsoleHelper.printBanner();
        AnsiConsoleHelper.printSection("🚀 Docker Hub Image Publisher");

        Path projectDir = Paths.get(projectPath).toAbsolutePath().normalize();
        DockerLocalService dockerService = new DockerLocalService();
        Scanner scanner = new Scanner(System.in);

        if (!dockerService.isDockerInstalled()) {
            AnsiConsoleHelper.printError("Docker daemon không được tìm thấy. Hãy cài đặt Docker trước.");
            return;
        }

        TechStackRuleEngine ruleEngine = new TechStackRuleEngine();
        ProjectConfig config = ruleEngine.analyzeLocalProject(projectDir);

        if (dockerUser != null && !dockerUser.trim().isEmpty()) {
            config.setDockerHubUsername(dockerUser.trim());
        }

        if (config.getDockerHubUsername() == null || config.getDockerHubUsername().isEmpty() || config.getDockerHubUsername().equals("myuser")) {
            System.out.print(AnsiConsoleHelper.BOLD + "Nhập Docker Hub Username: " + AnsiConsoleHelper.RESET);
            String user = scanner.nextLine().trim();
            if (!user.isEmpty()) config.setDockerHubUsername(user);
        }

        if (dockerToken == null || dockerToken.isEmpty()) {
            System.out.print(AnsiConsoleHelper.BOLD + "Nhập Docker Hub Token / Password (để trống nếu đã login trước đó): " + AnsiConsoleHelper.RESET);
            dockerToken = scanner.nextLine().trim();
        }

        String imageName = (customTag != null && !customTag.trim().isEmpty()) ? customTag.trim() : config.getFullDockerImageName();

        // 1. Đăng nhập nếu có token
        if (dockerToken != null && !dockerToken.isEmpty()) {
            boolean loggedIn = dockerService.loginDockerHub(config.getDockerHubUsername(), dockerToken, line -> System.out.print(line));
            if (!loggedIn) {
                AnsiConsoleHelper.printError("Đăng nhập thất bại.");
                return;
            }
        }

        // 2. Đẩy Image
        System.out.println(AnsiConsoleHelper.BOLD + "Đang đẩy Image: " + AnsiConsoleHelper.CYAN + imageName + AnsiConsoleHelper.RESET + "\n");
        boolean pushSuccess = dockerService.pushImage(imageName, line -> System.out.print(line));

        if (pushSuccess) {
            AnsiConsoleHelper.printSection("🎉 Xuất bản Thành công");
            AnsiConsoleHelper.printSuccess("Image đã có trên Docker Hub: " + imageName);
            System.out.println("\n" + AnsiConsoleHelper.GREEN + "👉 Triển khai ngay lên VPS với chế độ Docker Hub Pull:" + AnsiConsoleHelper.RESET);
            System.out.println("   " + AnsiConsoleHelper.BOLD + "easy-deploy deploy --mode registry_pull --docker-user " + config.getDockerHubUsername() + " --host <vps_ip>" + AnsiConsoleHelper.RESET);
            System.out.println();
        } else {
            AnsiConsoleHelper.printError("Đẩy Image lên Docker Hub thất bại.");
        }
    }
}
