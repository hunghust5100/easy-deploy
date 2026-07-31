package com.easydeploy.cli;

import com.easydeploy.cli.command.InitCommand;
import com.easydeploy.cli.command.ScanCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "easy-deploy",
    mixinStandardHelpOptions = true,
    version = "easy-deploy 1.0.0 (Pure Java 25)",
    description = "DevOps Configuration Generator CLI Tool for Web Applications",
    subcommands = {
        ScanCommand.class,
        InitCommand.class
    }
)
public class Main implements Runnable {

    @Override
    public void run() {
        System.out.println("┌────────────────────────────────────────────────────────┐");
        System.out.println("│  🚀 Easy-Deploy CLI v1.0.0 (Pure Java 25)               │");
        System.out.println("│  Automated DevOps Configuration Generator              │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println("\nUsage:");
        System.out.println("  easy-deploy scan       Auto-scan local folder & generate configs");
        System.out.println("  easy-deploy init       Interactive CLI wizard");
        System.out.println("  easy-deploy --help     Show help information");
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
