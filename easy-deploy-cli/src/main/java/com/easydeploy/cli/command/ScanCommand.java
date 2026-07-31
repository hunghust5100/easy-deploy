package com.easydeploy.cli.command;

import com.easydeploy.core.detector.TechStackRuleEngine;
import com.easydeploy.core.generator.FileWriterService;
import com.easydeploy.core.model.ProjectConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(
    name = "scan",
    description = "Scan current directory or target path to auto-detect stack & generate DevOps configs"
)
public class ScanCommand implements Runnable {

    @Option(names = {"-p", "--path"}, description = "Target project directory path", defaultValue = ".")
    private String targetPath;

    @Override
    public void run() {
        try {
            Path projectDir = Paths.get(targetPath).toAbsolutePath().normalize();
            System.out.println("🔍 Scanning directory: " + projectDir);

            if (!Files.exists(projectDir)) {
                System.err.println("❌ Directory does not exist: " + projectDir);
                return;
            }

            List<String> filePaths;
            try (Stream<Path> stream = Files.walk(projectDir, 3)) {
                filePaths = stream.map(p -> projectDir.relativize(p).toString())
                        .collect(Collectors.toList());
            }

            TechStackRuleEngine ruleEngine = new TechStackRuleEngine();
            ProjectConfig config = ruleEngine.analyzeFilePaths(filePaths);
            config.setAppName(projectDir.getFileName().toString());

            System.out.println("✔ Detected Tech Stack : " + config.getTechStack());
            System.out.println("✔ Detected App Port   : " + config.getAppPort());
            System.out.println("✔ Detected Database   : " + config.getDbType());

            System.out.println("\n🚀 Generating DevOps configuration files...");
            FileWriterService fileWriterService = new FileWriterService();
            fileWriterService.generateAll(config, projectDir);

            System.out.println("✅ Success! Generated Dockerfile, docker-compose.yml, nginx.conf in " + projectDir);
            System.out.println("👉 Run 'docker compose up -d' to start your environment.");

        } catch (Exception e) {
            System.err.println("❌ Failed to scan and generate configs: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
