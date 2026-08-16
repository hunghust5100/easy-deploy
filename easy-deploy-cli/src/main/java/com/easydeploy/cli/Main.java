package com.easydeploy.cli;

import com.easydeploy.cli.command.BuildCommand;
import com.easydeploy.cli.command.DeployCommand;
import com.easydeploy.cli.command.InitCommand;
import com.easydeploy.cli.command.PushCommand;
import com.easydeploy.cli.command.ScanCommand;
import com.easydeploy.cli.ui.AnsiConsoleHelper;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "easy-deploy",
    mixinStandardHelpOptions = true,
    version = "easy-deploy 1.0.0 (Pure Java 25)",
    description = "DevOps Configuration Generator & 1-Click SSH Deployment CLI Tool",
    subcommands = {
        ScanCommand.class,
        InitCommand.class,
        BuildCommand.class,
        PushCommand.class,
        DeployCommand.class
    }
)
public class Main implements Runnable {

    @Override
    public void run() {
        AnsiConsoleHelper.printBanner();
        System.out.println(AnsiConsoleHelper.BOLD + "Commands:" + AnsiConsoleHelper.RESET);
        System.out.println("  " + AnsiConsoleHelper.GREEN + "easy-deploy scan" + AnsiConsoleHelper.RESET + "       Quét sâu thư mục hiện tại & tự động sinh toàn bộ file DevOps");
        System.out.println("  " + AnsiConsoleHelper.GREEN + "easy-deploy init" + AnsiConsoleHelper.RESET + "       Chế độ Interactive Wizard cấu hình từng bước chi tiết");
        System.out.println("  " + AnsiConsoleHelper.GREEN + "easy-deploy build" + AnsiConsoleHelper.RESET + "      Đóng gói Docker Image cục bộ (Local Docker Build)");
        System.out.println("  " + AnsiConsoleHelper.GREEN + "easy-deploy push" + AnsiConsoleHelper.RESET + "       Đẩy Docker Image lên Docker Hub Registry");
        System.out.println("  " + AnsiConsoleHelper.GREEN + "easy-deploy deploy" + AnsiConsoleHelper.RESET + "     1-Click SSH Deploy ứng dụng lên máy chủ VPS (Build/Pull)");
        System.out.println("  " + AnsiConsoleHelper.GREEN + "easy-deploy --help" + AnsiConsoleHelper.RESET + "     Xem chi tiết các tùy chọn và cờ lệnh");
        System.out.println();
        System.out.println(AnsiConsoleHelper.DIM + "Ví dụ nhanh:" + AnsiConsoleHelper.RESET);
        System.out.println("  cd /path/to/my-project && easy-deploy scan");
        System.out.println("  easy-deploy build --tag myuser/myapp:latest");
        System.out.println("  easy-deploy push --tag myuser/myapp:latest");
        System.out.println("  easy-deploy deploy --mode registry_pull --host 103.179.x.x --user root");
        System.out.println();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
