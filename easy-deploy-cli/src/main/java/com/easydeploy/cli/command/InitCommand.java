package com.easydeploy.cli.command;

import com.easydeploy.core.generator.FileWriterService;
import com.easydeploy.core.model.ProjectConfig;
import picocli.CommandLine.Command;

import java.nio.file.Paths;
import java.util.Scanner;

@Command(
    name = "init",
    description = "Interactive CLI Wizard to step-by-step configure and generate DevOps files"
)
public class InitCommand implements Runnable {

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        ProjectConfig config = new ProjectConfig();

        System.out.println("🧙 Welcome to Easy-Deploy Interactive Wizard!\n");

        System.out.print("Enter App Name [default: my-app]: ");
        String appName = scanner.nextLine().trim();
        if (!appName.isEmpty()) config.setAppName(appName);

        System.out.print("Select Tech Stack (1: Java Spring Boot, 2: Node.js, 3: Python) [default: 1]: ");
        String stackChoice = scanner.nextLine().trim();
        if ("2".equals(stackChoice)) {
            config.setTechStack("NODE_BACKEND");
            config.setAppPort(3000);
        } else if ("3".equals(stackChoice)) {
            config.setTechStack("PYTHON");
            config.setAppPort(8000);
        } else {
            config.setTechStack("JAVA_MAVEN");
            config.setAppPort(8080);
        }

        System.out.print("Enter App Port [default: " + config.getAppPort() + "]: ");
        String portInput = scanner.nextLine().trim();
        if (!portInput.isEmpty()) {
            try { config.setAppPort(Integer.parseInt(portInput)); } catch (NumberFormatException ignored) {}
        }

        System.out.print("Select Database (1: PostgreSQL, 2: MySQL, 3: None) [default: 1]: ");
        String dbChoice = scanner.nextLine().trim();
        if ("2".equals(dbChoice)) {
            config.setDbType("MYSQL");
            config.setDbPort(3306);
        } else if ("3".equals(dbChoice)) {
            config.setDbType("NONE");
        } else {
            config.setDbType("POSTGRESQL");
            config.setDbPort(5432);
        }

        System.out.print("Enable Nginx Reverse Proxy? (Y/n) [default: Y]: ");
        String nginxInput = scanner.nextLine().trim();
        if ("n".equalsIgnoreCase(nginxInput)) config.setEnableNginx(false);

        try {
            System.out.println("\n🚀 Generating DevOps configuration files...");
            FileWriterService fileWriterService = new FileWriterService();
            fileWriterService.generateAll(config, Paths.get("."));

            System.out.println("✅ All files generated successfully in current directory!");
            System.out.println("👉 Run 'docker compose up -d' to start your application.");
        } catch (Exception e) {
            System.err.println("❌ Error generating files: " + e.getMessage());
        }
    }
}
