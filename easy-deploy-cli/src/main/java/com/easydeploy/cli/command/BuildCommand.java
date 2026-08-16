package com.easydeploy.cli.command;

import com.easydeploy.cli.ui.AnsiConsoleHelper;
import com.easydeploy.core.detector.TechStackRuleEngine;
import com.easydeploy.core.docker.DockerLocalService;
import com.easydeploy.core.generator.FileWriterService;
import com.easydeploy.core.model.ProjectConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Command(
    name = "build",
    mixinStandardHelpOptions = true,
    description = "Build a local Docker image for the current project"
)
public class BuildCommand implements Runnable {

    @Option(names = {"-t", "--tag"}, description = "Docker image tag or full name (e.g. myuser/myapp:latest)")
    private String customTag;

    @Option(names = {"-u", "--user"}, description = "Docker Hub username (e.g. myuser)")
    private String dockerUser;

    @Option(names = {"-p", "--path"}, description = "Project directory path", defaultValue = ".")
    private String projectPath;

    @Override
    public void run() {
        AnsiConsoleHelper.printBanner();
        AnsiConsoleHelper.printSection("📦 Docker Local Image Builder");

        Path projectDir = Paths.get(projectPath).toAbsolutePath().normalize();
        DockerLocalService dockerService = new DockerLocalService();

        if (!dockerService.isDockerInstalled()) {
            AnsiConsoleHelper.printError("Docker daemon không được tìm thấy trên máy của bạn. Hãy cài đặt Docker Desktop hoặc Docker Engine trước.");
            return;
        }

        TechStackRuleEngine ruleEngine = new TechStackRuleEngine();
        ProjectConfig config = ruleEngine.analyzeLocalProject(projectDir);

        if (dockerUser != null && !dockerUser.trim().isEmpty()) {
            config.setDockerHubUsername(dockerUser.trim());
        }

        // Tự động sinh Dockerfile nếu chưa có
        if (!Files.exists(projectDir.resolve("Dockerfile"))) {
            AnsiConsoleHelper.printInfo("Không tìm thấy Dockerfile. Đang tự động phân tích stack và sinh cấu hình...");
            try {
                FileWriterService writer = new FileWriterService();
                writer.generateAll(config, projectDir, true, true);
                AnsiConsoleHelper.printSuccess("Đã tự động tạo Dockerfile và các tệp liên quan.");
            } catch (Exception e) {
                AnsiConsoleHelper.printError("Không thể sinh Dockerfile: " + e.getMessage());
                return;
            }
        }

        String imageName = (customTag != null && !customTag.trim().isEmpty()) ? customTag.trim() : config.getFullDockerImageName();
        System.out.println(AnsiConsoleHelper.BOLD + "Image Name : " + AnsiConsoleHelper.CYAN + imageName + AnsiConsoleHelper.RESET);
        System.out.println(AnsiConsoleHelper.BOLD + "Working Dir: " + AnsiConsoleHelper.DIM + projectDir + AnsiConsoleHelper.RESET);
        System.out.println();

        boolean success = dockerService.buildImage(projectDir, imageName, line -> System.out.print(line));
        if (success) {
            AnsiConsoleHelper.printSection("🎉 Build Thành công");
            AnsiConsoleHelper.printSuccess("Image đã sẵn sàng: " + imageName);
            System.out.println("\n" + AnsiConsoleHelper.GREEN + "👉 Bạn có thể đẩy image lên Docker Hub bằng lệnh:" + AnsiConsoleHelper.RESET);
            System.out.println("   " + AnsiConsoleHelper.BOLD + "easy-deploy push --tag " + imageName + AnsiConsoleHelper.RESET);
            System.out.println();
        } else {
            AnsiConsoleHelper.printError("Đóng gói Docker Image thất bại. Xem log chi tiết ở trên.");
        }
    }
}
