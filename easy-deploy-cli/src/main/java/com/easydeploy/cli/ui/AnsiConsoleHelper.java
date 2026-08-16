package com.easydeploy.cli.ui;

import com.easydeploy.core.model.ProjectConfig;

public class AnsiConsoleHelper {

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static void printBanner() {
        System.out.println(CYAN + BOLD + "  ______                       _____             _             " + RESET);
        System.out.println(CYAN + BOLD + " |  ____|                     |  __ \\           | |            " + RESET);
        System.out.println(CYAN + BOLD + " | |__   __ _ ___ _   _ ______| |  | | ___ _ __ | | ___  _   _ " + RESET);
        System.out.println(CYAN + BOLD + " |  __| / _` / __| | | |______| |  | |/ _ \\ '_ \\| |/ _ \\| | | |" + RESET);
        System.out.println(CYAN + BOLD + " | |___| (_| \\__ \\ |_| |      | |__| |  __/ |_) | | (_) | |_| |" + RESET);
        System.out.println(CYAN + BOLD + " |______\\__,_|___/\\__, |      |_____/ \\___| .__/|_|\\___/ \\__, |" + RESET);
        System.out.println(CYAN + BOLD + "                   __/ |                  | |             __/ |" + RESET);
        System.out.println(CYAN + BOLD + "                  |___/                   |_|            |___/ " + RESET);
        System.out.println(DIM + " » DevOps Configuration Generator & 1-Click SSH Deployment CLI" + RESET);
        System.out.println();
    }

    public static void printSection(String title) {
        System.out.println("\n" + CYAN + BOLD + "── " + title + " " + "─".repeat(Math.max(0, 50 - title.length())) + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + BOLD + "✔ " + RESET + GREEN + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(CYAN + "ℹ " + RESET + message);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + "⚠ " + RESET + YELLOW + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + BOLD + "✖ " + message + RESET);
    }

    public static void printConfigTable(ProjectConfig config) {
        System.out.println(BOLD + "┌──────────────────────┬──────────────────────────────────────────┐" + RESET);
        System.out.println(BOLD + "│ " + CYAN + "Thuộc tính (Option)" + RESET + "  │ " + CYAN + "Giá trị Đề xuất (Suggested Value)" + RESET + "        │");
        System.out.println(BOLD + "├──────────────────────┼──────────────────────────────────────────┤" + RESET);
        printTableRow("App Name", config.getAppName());
        printTableRow("Tech Stack", config.getTechStack());
        printTableRow("Runtime Version", config.getTechVersion() != null ? config.getTechVersion() : "Auto");
        printTableRow("Container Port", String.valueOf(config.getAppPort()));
        printTableRow("Host Port", String.valueOf(config.getHostPort()));
        printTableRow("Database", config.getDbType() + (config.getDbType().equals("NONE") ? "" : " (Port " + config.getDbPort() + ")"));
        printTableRow("Nginx Proxy", config.isEnableNginx() ? "Enabled (Port 80/443)" : "Disabled");
        printTableRow("Domain / Host", config.getDomainName() != null ? config.getDomainName() : "localhost");
        printTableRow("GitHub CI/CD", config.isEnableCicd() ? "Enabled (.github/workflows)" : "Disabled");
        printTableRow("Deploy Mode", "registry_pull".equals(config.getDeployMode()) ? "Docker Hub Pull" : "Remote Build on VPS");
        if ("registry_pull".equals(config.getDeployMode()) || config.isUseDockerHub()) {
            printTableRow("Docker Image", config.getFullDockerImageName());
        }
        printTableRow("Server Setup Script", config.isEnableServerSetup() ? "Enabled (setup-server.sh)" : "Auto (Ready)");
        System.out.println(BOLD + "└──────────────────────┴──────────────────────────────────────────┘" + RESET);
    }

    private static void printTableRow(String key, String value) {
        String paddedKey = String.format("%-20s", key);
        String paddedVal = String.format("%-40s", (value.length() > 40 ? value.substring(0, 37) + "..." : value));
        System.out.println("│ " + WHITE + paddedKey + RESET + " │ " + GREEN + paddedVal + RESET + " │");
    }
}
