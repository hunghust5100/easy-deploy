package com.easydeploy.core.model.enums;

public enum TechStack {
    JAVA_MAVEN("☕ Java Spring Boot (Maven)", "21", 8080),
    JAVA_GRADLE("🐘 Java Spring Boot (Gradle)", "21", 8080),
    NODE_FRONTEND("⚛️ Node.js Frontend (React / Vite / Vue)", "20", 3000),
    NODE_BACKEND("🟢 Node.js Backend (Express / NestJS / Fastify)", "20", 3000),
    NEXTJS_FULLSTACK("⚡ Next.js (SSR / Fullstack Standalone)", "20", 3000),
    PYTHON("🐍 Python (FastAPI / Django / Flask)", "3.11", 8000),
    GO("🐹 Go (Golang Static Binary)", "1.22", 8080),
    RUST("🦀 Rust (Cargo Release)", "1.78", 8080),
    PHP_LARAVEL("🐘 PHP (Laravel / Symfony / Composer)", "8.2", 8000),
    DOTNET("🔷 .NET / C# (ASP.NET Core 8.0)", "8.0", 8080),
    RUBY_RAILS("💎 Ruby on Rails (Puma Web Server)", "3.3", 3000);

    private final String displayName;
    private final String defaultVersion;
    private final int defaultPort;

    TechStack(String displayName, String defaultVersion, int defaultPort) {
        this.displayName = displayName;
        this.defaultVersion = defaultVersion;
        this.defaultPort = defaultPort;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultVersion() {
        return defaultVersion;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public static TechStack fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return JAVA_MAVEN;
        }
        for (TechStack stack : TechStack.values()) {
            if (stack.name().equalsIgnoreCase(text.trim())) {
                return stack;
            }
        }
        // Aliases
        if ("GOLANG".equalsIgnoreCase(text.trim())) return GO;
        if ("CSHARP".equalsIgnoreCase(text.trim())) return DOTNET;
        if ("PHP".equalsIgnoreCase(text.trim())) return PHP_LARAVEL;
        if ("RUBY".equalsIgnoreCase(text.trim())) return RUBY_RAILS;

        return JAVA_MAVEN;
    }
}
