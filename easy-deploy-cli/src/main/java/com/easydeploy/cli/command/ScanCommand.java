package com.easydeploy.cli.command;

import com.easydeploy.cli.ui.AnsiConsoleHelper;
import com.easydeploy.core.detector.TechStackRuleEngine;
import com.easydeploy.core.generator.FileWriterService;
import com.easydeploy.core.generator.FileWriterService.GenerationSummary;
import com.easydeploy.core.generator.FileWriterService.GeneratedFile;
import com.easydeploy.core.model.ProjectConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

@Command(
    name = "scan",
    mixinStandardHelpOptions = true,
    description = "Scan directory, auto-detect Tech Stack & generate production DevOps configurations"
)
public class ScanCommand implements Runnable {

    @Option(names = {"-p", "--path"}, description = "Target project directory path (default: current directory)", defaultValue = ".")
    private String targetPath;

    @Option(names = {"-y", "--yes"}, description = "Auto-accept detected configuration and generate without interactive prompt")
    private boolean autoYes;

    @Option(names = {"-f", "--force"}, description = "Force overwrite existing configuration files without confirmation")
    private boolean forceOverwrite;

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(targetPath).toAbsolutePath().normalize();
            AnsiConsoleHelper.printBanner();
            AnsiConsoleHelper.printSection("🔍 Auto-Scanning Project Directory");
            System.out.println(AnsiConsoleHelper.DIM + "Target: " + projectDir + AnsiConsoleHelper.RESET);

            if (!Files.exists(projectDir) || !Files.isDirectory(projectDir)) {
                AnsiConsoleHelper.printError("Directory does not exist: " + projectDir);
                return;
            }

            // 1. Deep Project Scan & Detection
            System.out.println(AnsiConsoleHelper.CYAN + "Scanning project tree and parsing configuration files..." + AnsiConsoleHelper.RESET);
            TechStackRuleEngine ruleEngine = new TechStackRuleEngine();
            ProjectConfig config = ruleEngine.analyzeLocalProject(projectDir);

            // 2. Display Result Table
            AnsiConsoleHelper.printSection("📋 Detected Tech Stack & Recommended Configuration");
            AnsiConsoleHelper.printConfigTable(config);

            // 3. Interactive Prompt (unless -y / --yes flag is given)
            if (!autoYes) {
                System.out.print("\n" + AnsiConsoleHelper.BOLD + "👉 Press [" + AnsiConsoleHelper.GREEN + "Enter" + AnsiConsoleHelper.WHITE + "] to generate files, [" + AnsiConsoleHelper.YELLOW + "c" + AnsiConsoleHelper.WHITE + "] to customize, [" + AnsiConsoleHelper.RED + "q" + AnsiConsoleHelper.WHITE + "] to cancel: " + AnsiConsoleHelper.RESET);
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine().trim();

                if ("q".equalsIgnoreCase(input)) {
                    AnsiConsoleHelper.printWarning("Operation cancelled by user.");
                    return;
                } else if ("c".equalsIgnoreCase(input)) {
                    customizeConfig(config, scanner);
                }
            }

            // 4. Generate Files
            AnsiConsoleHelper.printSection("🚀 Generating Production DevOps Files");
            FileWriterService writer = new FileWriterService();
            GenerationSummary summary = writer.generateAll(config, projectDir, forceOverwrite, true);

            for (GeneratedFile file : summary.getFiles()) {
                switch (file.getStatus()) {
                    case "CREATED":
                        AnsiConsoleHelper.printSuccess("Created : " + file.getRelativePath());
                        break;
                    case "OVERWRITTEN":
                        AnsiConsoleHelper.printWarning("Overwritten : " + file.getRelativePath());
                        break;
                    case "BACKED_UP":
                        AnsiConsoleHelper.printInfo("Backup : " + file.getRelativePath());
                        break;
                    case "SKIPPED":
                        AnsiConsoleHelper.printWarning("Skipped (already exists) : " + file.getRelativePath() + " (use -f to overwrite)");
                        break;
                }
            }

            // 5. Next steps
            AnsiConsoleHelper.printSection("✨ Next Steps");
            System.out.println(AnsiConsoleHelper.GREEN + "1. Chạy thử nghiệm Local Container:" + AnsiConsoleHelper.RESET);
            System.out.println("   " + AnsiConsoleHelper.BOLD + "docker compose up -d --build" + AnsiConsoleHelper.RESET);
            System.out.println(AnsiConsoleHelper.GREEN + "2. Triển khai 1-Click SSH lên máy chủ VPS:" + AnsiConsoleHelper.RESET);
            System.out.println("   " + AnsiConsoleHelper.BOLD + "easy-deploy deploy --host <vps_ip> --user root" + AnsiConsoleHelper.RESET);
            System.out.println();

        } catch (Exception e) {
            AnsiConsoleHelper.printError("Failed to scan and generate configurations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void customizeConfig(ProjectConfig config, Scanner scanner) {
        AnsiConsoleHelper.printSection("⚙️ Customize Project Configuration");

        System.out.print("App Name [" + config.getAppName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) config.setAppName(name);

        System.out.print("Container Port [" + config.getAppPort() + "]: ");
        String port = scanner.nextLine().trim();
        if (!port.isEmpty()) {
            try { config.setAppPort(Integer.parseInt(port)); } catch (Exception ignored) {}
        }

        System.out.print("Host Port [" + config.getHostPort() + "]: ");
        String hostPort = scanner.nextLine().trim();
        if (!hostPort.isEmpty()) {
            try { config.setHostPort(Integer.parseInt(hostPort)); } catch (Exception ignored) {}
        }

        System.out.print("Database Type (POSTGRESQL, MYSQL, MARIADB, MONGODB, REDIS, NONE) [" + config.getDbType() + "]: ");
        String db = scanner.nextLine().trim().toUpperCase();
        if (!db.isEmpty()) config.setDbType(db);

        System.out.print("Enable Nginx Reverse Proxy? (y/n) [" + (config.isEnableNginx() ? "y" : "n") + "]: ");
        String nginx = scanner.nextLine().trim();
        if ("n".equalsIgnoreCase(nginx)) config.setEnableNginx(false);
        else if ("y".equalsIgnoreCase(nginx)) config.setEnableNginx(true);

        System.out.print("Domain Name [" + config.getDomainName() + "]: ");
        String domain = scanner.nextLine().trim();
        if (!domain.isEmpty()) config.setDomainName(domain);
    }
}
