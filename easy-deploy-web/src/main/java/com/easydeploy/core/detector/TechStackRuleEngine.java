package com.easydeploy.core.detector;

import com.easydeploy.core.model.ProjectConfig;
import com.easydeploy.core.model.ServiceModule;
import com.easydeploy.core.model.enums.DbType;
import com.easydeploy.core.model.enums.ServiceType;
import com.easydeploy.core.model.enums.TechStack;
import com.easydeploy.core.parser.FileContentParser;
import com.easydeploy.core.scanner.GithubTreeScanner.ScanResult;
import com.easydeploy.core.scanner.LocalTreeScanner;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TechStackRuleEngine {

    private final FileContentParser parser;
    private final LocalTreeScanner localScanner;

    public TechStackRuleEngine() {
        this.parser = new FileContentParser();
        this.localScanner = new LocalTreeScanner();
    }

    public ProjectConfig analyzeFilePaths(List<String> relativePaths) {
        ProjectConfig config = new ProjectConfig();
        if (relativePaths == null || relativePaths.isEmpty()) {
            return config;
        }

        // 1. Detect all submodule packages in the repository
        List<ServiceModule> detectedServices = detectServiceModules(relativePaths);
        config.setServices(detectedServices);

        // 2. Determine primary overall tech stack & ports
        Optional<ServiceModule> backendOpt = config.getBackendService();
        Optional<ServiceModule> frontendOpt = config.getFrontendService();

        if (backendOpt.isPresent()) {
            ServiceModule primaryBackend = backendOpt.get();
            config.setTechStack(primaryBackend.getTechStack());
            config.setTechVersion(primaryBackend.getTechVersion());
            config.setAppPort(primaryBackend.getContainerPort());
            config.setHostPort(primaryBackend.getHostPort());
        } else if (!detectedServices.isEmpty()) {
            ServiceModule first = detectedServices.get(0);
            config.setTechStack(first.getTechStack());
            config.setTechVersion(first.getTechVersion());
            config.setAppPort(first.getContainerPort());
            config.setHostPort(first.getHostPort());
        } else {
            // Fallback default
            config.setTechStack(TechStack.JAVA_MAVEN.name());
            config.setTechVersion(TechStack.JAVA_MAVEN.getDefaultVersion());
            config.setAppPort(TechStack.JAVA_MAVEN.getDefaultPort());
            config.setHostPort(8080);
        }

        // 3. Detect Database Drivers & Services from file path names
        boolean hasPostgres = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("postgres"));
        boolean hasMysql = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("mysql"));
        boolean hasMaria = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("mariadb"));
        boolean hasMongo = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("mongo"));
        boolean hasRedis = relativePaths.stream().anyMatch(p -> p.toLowerCase().contains("redis"));

        if (hasPostgres) {
            config.setDbType(DbType.POSTGRESQL.name());
            config.setDbPort(DbType.POSTGRESQL.getDefaultPort());
        } else if (hasMysql) {
            config.setDbType(DbType.MYSQL.name());
            config.setDbPort(DbType.MYSQL.getDefaultPort());
        } else if (hasMaria) {
            config.setDbType(DbType.MARIADB.name());
            config.setDbPort(DbType.MARIADB.getDefaultPort());
        } else if (hasMongo) {
            config.setDbType(DbType.MONGODB.name());
            config.setDbPort(DbType.MONGODB.getDefaultPort());
        } else if (hasRedis) {
            config.setDbType(DbType.REDIS.name());
            config.setDbPort(DbType.REDIS.getDefaultPort());
        } else {
            config.setDbType(DbType.POSTGRESQL.name());
            config.setDbPort(DbType.POSTGRESQL.getDefaultPort());
        }

        config.setEnableNginx(true);
        config.setEnableCicd(false);

        return config;
    }

    /**
     * Dò tìm tất cả các module/package con có mặt trong cây thư mục
     */
    public List<ServiceModule> detectServiceModules(List<String> relativePaths) {
        if (relativePaths == null || relativePaths.isEmpty()) {
            return new ArrayList<>();
        }

        // Tập hợp các thư mục chứa anchor file
        Map<String, List<String>> folderToAnchorFiles = new LinkedHashMap<>();

        List<String> anchorNames = List.of(
                "pom.xml", "build.gradle", "build.gradle.kts",
                "package.json", "requirements.txt", "pyproject.toml", "Pipfile",
                "go.mod", "Cargo.toml", "composer.json", "Gemfile"
        );

        for (String filePath : relativePaths) {
            String norm = filePath.replace("\\", "/");
            int lastSlash = norm.lastIndexOf('/');
            String dir = (lastSlash >= 0) ? norm.substring(0, lastSlash) : ".";
            String fileName = (lastSlash >= 0) ? norm.substring(lastSlash + 1) : norm;

            if (anchorNames.contains(fileName) || fileName.endsWith(".csproj")) {
                folderToAnchorFiles.computeIfAbsent(dir, k -> new ArrayList<>()).add(fileName);
            }
        }

        // Loại bỏ các thư mục lồng nhau không phải là submodule chính (như src/, docs/, etc.)
        List<String> validSubDirs = new ArrayList<>();
        for (String dir : folderToAnchorFiles.keySet()) {
            if (!".".equals(dir) && !dir.contains("/src") && !dir.contains("/test") && !dir.contains("/dist") && !dir.contains("/build") && !dir.contains("/target")) {
                validSubDirs.add(dir);
            }
        }

        List<String> validDirs = new ArrayList<>(validSubDirs);
        if (folderToAnchorFiles.containsKey(".")) {
            // Nếu root có anchor, kiểm tra xem có subproject backend nào khác hay không
            boolean hasOtherBackendSub = validSubDirs.stream().anyMatch(d -> {
                List<String> a = folderToAnchorFiles.get(d);
                return a != null && (a.contains("build.gradle") || a.contains("build.gradle.kts") || a.contains("pom.xml") || a.contains("go.mod") || a.contains("requirements.txt") || a.contains("Cargo.toml"));
            });

            // Nếu không có backend subproject con nào (ví dụ chỉ có frontend subproject hoặc không có subproject), giữ lại root làm backend service
            if (!hasOtherBackendSub) {
                validDirs.add(0, ".");
            }
        }

        List<ServiceModule> services = new ArrayList<>();
        Set<Integer> allocatedHostPorts = new HashSet<>();

        for (String dir : validDirs) {
            List<String> anchors = folderToAnchorFiles.get(dir);
            List<String> dirFiles = relativePaths.stream()
                    .filter(p -> ".".equals(dir) ? !p.contains("/") : p.startsWith(dir + "/"))
                    .collect(Collectors.toList());

            ServiceModule module = createServiceModuleFromDirectory(dir, anchors, dirFiles, relativePaths);
            if (module != null) {
                // Điều chỉnh hostPort tránh trùng lặp giữa các services
                int targetHostPort = module.getHostPort();
                while (allocatedHostPorts.contains(targetHostPort)) {
                    targetHostPort++;
                }
                module.setHostPort(targetHostPort);
                allocatedHostPorts.add(targetHostPort);

                services.add(module);
            }
        }

        // Nếu không phát hiện submodule nào nhưng có files chung
        if (services.isEmpty()) {
            ServiceModule defaultModule = new ServiceModule(
                    "app-main", "app-main", ".", ServiceType.BACKEND,
                    TechStack.JAVA_MAVEN.name(), "21", 8080, 8080
            );
            services.add(defaultModule);
        }

        return services;
    }

    private ServiceModule createServiceModuleFromDirectory(String dir, List<String> anchors, List<String> dirFiles, List<String> allFiles) {
        String serviceId = ".".equals(dir) ? "root-app" : dir.replaceAll("[^a-zA-Z0-9_-]", "-");
        String serviceName = ".".equals(dir) ? "main-service" : dir.substring(dir.lastIndexOf('/') + 1).toLowerCase();

        boolean hasPom = anchors.contains("pom.xml");
        boolean hasGradle = anchors.contains("build.gradle") || anchors.contains("build.gradle.kts");
        boolean hasPackageJson = anchors.contains("package.json");
        boolean hasRequirements = anchors.contains("requirements.txt") || anchors.contains("pyproject.toml") || anchors.contains("Pipfile");
        boolean hasGoMod = anchors.contains("go.mod");
        boolean hasCargo = anchors.contains("Cargo.toml");
        boolean hasComposer = anchors.contains("composer.json");
        boolean hasRuby = anchors.contains("Gemfile");
        boolean hasDotnet = anchors.stream().anyMatch(a -> a.endsWith(".csproj"));

        ServiceType serviceType = ServiceType.BACKEND;
        String techStack = TechStack.JAVA_MAVEN.name();
        String techVersion = "21";
        int containerPort = 8080;
        int hostPort = 8080;
        String buildCommand = "";

        if (hasPom) {
            techStack = TechStack.JAVA_MAVEN.name();
            techVersion = TechStack.JAVA_MAVEN.getDefaultVersion();
            containerPort = TechStack.JAVA_MAVEN.getDefaultPort();
            hostPort = 8080;
            buildCommand = "./mvnw clean package -DskipTests";
            serviceType = ServiceType.BACKEND;
        } else if (hasGradle) {
            techStack = TechStack.JAVA_GRADLE.name();
            techVersion = TechStack.JAVA_GRADLE.getDefaultVersion();
            containerPort = TechStack.JAVA_GRADLE.getDefaultPort();
            hostPort = 8080;
            buildCommand = "./gradlew bootJar";
            serviceType = ServiceType.BACKEND;
        } else if (hasDotnet) {
            techStack = TechStack.DOTNET.name();
            techVersion = TechStack.DOTNET.getDefaultVersion();
            containerPort = TechStack.DOTNET.getDefaultPort();
            hostPort = 8080;
            buildCommand = "dotnet publish -c Release -o out";
            serviceType = ServiceType.BACKEND;
        } else if (hasComposer) {
            techStack = TechStack.PHP_LARAVEL.name();
            techVersion = TechStack.PHP_LARAVEL.getDefaultVersion();
            containerPort = TechStack.PHP_LARAVEL.getDefaultPort();
            hostPort = 8000;
            serviceType = ServiceType.BACKEND;
        } else if (hasRuby) {
            techStack = TechStack.RUBY_RAILS.name();
            techVersion = TechStack.RUBY_RAILS.getDefaultVersion();
            containerPort = TechStack.RUBY_RAILS.getDefaultPort();
            hostPort = 3000;
            serviceType = ServiceType.BACKEND;
        } else if (hasGoMod) {
            techStack = TechStack.GO.name();
            techVersion = TechStack.GO.getDefaultVersion();
            containerPort = TechStack.GO.getDefaultPort();
            hostPort = 8080;
            buildCommand = "go build -o main .";
            serviceType = ServiceType.BACKEND;
        } else if (hasCargo) {
            techStack = TechStack.RUST.name();
            techVersion = TechStack.RUST.getDefaultVersion();
            containerPort = TechStack.RUST.getDefaultPort();
            hostPort = 8080;
            buildCommand = "cargo build --release";
            serviceType = ServiceType.BACKEND;
        } else if (hasRequirements) {
            techStack = TechStack.PYTHON.name();
            techVersion = TechStack.PYTHON.getDefaultVersion();
            containerPort = TechStack.PYTHON.getDefaultPort();
            hostPort = 8000;
            serviceType = ServiceType.BACKEND;
        } else if (hasPackageJson) {
            String dirLower = dir.toLowerCase();
            boolean isFrontendFolder = dirLower.contains("frontend") || dirLower.contains("client") || dirLower.contains("ui") || dirLower.contains("web");
            boolean hasVite = dirFiles.stream().anyMatch(p -> p.contains("vite.config"));
            boolean hasNext = dirFiles.stream().anyMatch(p -> p.contains("next.config"));
            boolean hasReactOrVue = dirFiles.stream().anyMatch(p -> p.endsWith(".jsx") || p.endsWith(".tsx") || p.endsWith(".vue"));

            if (hasNext) {
                techStack = TechStack.NEXTJS_FULLSTACK.name();
                techVersion = TechStack.NEXTJS_FULLSTACK.getDefaultVersion();
                containerPort = TechStack.NEXTJS_FULLSTACK.getDefaultPort();
                hostPort = 3000;
                buildCommand = "npm run build";
                serviceType = ServiceType.FULLSTACK;
            } else if (hasVite || isFrontendFolder || hasReactOrVue) {
                techStack = TechStack.NODE_FRONTEND.name();
                techVersion = TechStack.NODE_FRONTEND.getDefaultVersion();
                containerPort = TechStack.NODE_FRONTEND.getDefaultPort();
                hostPort = 3000;
                buildCommand = "npm run build";
                serviceType = ServiceType.FRONTEND;
            } else {
                techStack = TechStack.NODE_BACKEND.name();
                techVersion = TechStack.NODE_BACKEND.getDefaultVersion();
                containerPort = TechStack.NODE_BACKEND.getDefaultPort();
                hostPort = 3000;
                buildCommand = "npm run build";
                serviceType = ServiceType.BACKEND;
            }
        }

        ServiceModule module = new ServiceModule(
                serviceId, serviceName, dir, serviceType, techStack, techVersion, containerPort, hostPort
        );
        module.setBuildCommand(buildCommand);

        // Đối với các submodule mang tính chất thư viện/utility/CLI nội bộ trong monorepo
        String dirLower = dir.toLowerCase();
        if (dirLower.contains("core") || dirLower.contains("common") || dirLower.contains("util") || dirLower.contains("lib") || dirLower.contains("shared") || dirLower.contains("cli")) {
            module.setEnabled(false);
        }

        return module;
    }

    public ProjectConfig enrichWithFileContents(ProjectConfig config, Map<String, String> fileContents) {
        if (config == null || fileContents == null || fileContents.isEmpty()) {
            return config;
        }

        boolean isJava = "JAVA_MAVEN".equals(config.getTechStack()) || "JAVA_GRADLE".equals(config.getTechStack());
        boolean isPython = "PYTHON".equals(config.getTechStack());
        boolean isGo = "GO".equals(config.getTechStack());
        boolean isRust = "RUST".equals(config.getTechStack());
        boolean isDotnet = "DOTNET".equals(config.getTechStack());
        boolean isPhp = "PHP_LARAVEL".equals(config.getTechStack());
        boolean isOtherBackend = isJava || isPython || isGo || isRust || isDotnet || isPhp;

        // 1. Check POM / Gradle for Java Version and Database
        String pomContent = fileContents.get("pom.xml");
        if (pomContent != null && !pomContent.isEmpty()) {
            String v = parser.parseJavaVersion(pomContent);
            if (v != null) config.setTechVersion(v);
            String db = parser.detectDatabaseFromContent(pomContent);
            if (db != null) {
                config.setDbType(db);
                config.setDbPort(DbType.fromString(db).getDefaultPort());
            }
        }

        String gradleContent = fileContents.get("build.gradle");
        if (gradleContent == null) gradleContent = fileContents.get("build.gradle.kts");
        if (gradleContent != null && !gradleContent.isEmpty()) {
            String v = parser.parseJavaVersion(gradleContent);
            if (v != null) config.setTechVersion(v);
            String db = parser.detectDatabaseFromContent(gradleContent);
            if (db != null) {
                config.setDbType(db);
                config.setDbPort(DbType.fromString(db).getDefaultPort());
            }
        }

        // 2. Check application.yml / properties for port and database
        String appYml = fileContents.get("src/main/resources/application.yml");
        if (appYml == null) appYml = fileContents.get("src/main/resources/application.yaml");
        if (appYml != null) {
            Integer port = parser.parseServerPort(appYml);
            if (port != null) {
                config.setAppPort(port);
                config.setHostPort(port);
            }
            String db = parser.detectDatabaseFromContent(appYml);
            if (db != null) {
                config.setDbType(db);
                config.setDbPort(DbType.fromString(db).getDefaultPort());
            }
        }

        String appProp = fileContents.get("src/main/resources/application.properties");
        if (appProp != null) {
            Integer port = parser.parseServerPort(appProp);
            if (port != null) {
                config.setAppPort(port);
                config.setHostPort(port);
            }
            String db = parser.detectDatabaseFromContent(appProp);
            if (db != null) {
                config.setDbType(db);
                config.setDbPort(DbType.fromString(db).getDefaultPort());
            }
        }

        // 3. Check package.json
        String pkgJson = fileContents.get("package.json");
        if (pkgJson != null) {
            String db = parser.detectDatabaseFromContent(pkgJson);
            if (db != null && !isJava) {
                config.setDbType(db);
                config.setDbPort(DbType.fromString(db).getDefaultPort());
            }

            if (!isOtherBackend) {
                if (pkgJson.contains("\"next\"")) {
                    config.setTechStack("NEXTJS_FULLSTACK");
                    config.setTechVersion("20");
                    config.setAppPort(3000);
                    config.setHostPort(3000);
                } else if (pkgJson.contains("\"vite\"") || pkgJson.contains("\"react\"") || pkgJson.contains("\"vue\"")) {
                    config.setTechStack("NODE_FRONTEND");
                    config.setTechVersion("20");
                    config.setAppPort(3000);
                    config.setHostPort(3000);
                } else if (pkgJson.contains("\"express\"") || pkgJson.contains("\"@nestjs/core\"") || pkgJson.contains("\"fastify\"") || pkgJson.contains("\"koa\"")) {
                    config.setTechStack("NODE_BACKEND");
                    config.setTechVersion("20");
                    config.setAppPort(3000);
                    config.setHostPort(3000);
                }
            }
        }

        // 4. Check requirements.txt for Python
        String reqTxt = fileContents.get("requirements.txt");
        if (reqTxt != null) {
            String db = parser.detectDatabaseFromContent(reqTxt);
            if (db != null && !isJava) {
                config.setDbType(db);
                config.setDbPort(DbType.fromString(db).getDefaultPort());
            }
            if (isPython) {
                if (reqTxt.contains("fastapi") || reqTxt.contains("uvicorn")) {
                    config.setAppPort(8000);
                    config.setHostPort(8000);
                } else if (reqTxt.contains("flask")) {
                    config.setAppPort(5000);
                    config.setHostPort(5000);
                } else if (reqTxt.contains("django")) {
                    config.setAppPort(8000);
                    config.setHostPort(8000);
                }
            }
        }

        // 5. Update services in config with enriched info
        for (ServiceModule sm : config.getServices()) {
            if (".".equals(sm.getRelativePath()) || sm.getTechStack().equals(config.getTechStack())) {
                sm.setTechVersion(config.getTechVersion());
                sm.setContainerPort(config.getAppPort());
            }
        }

        return config;
    }

    public ProjectConfig analyzeGithubScan(ScanResult scanResult) {
        ProjectConfig config = analyzeFilePaths(scanResult.getFilePaths());
        return enrichWithFileContents(config, scanResult.getKeyFilesContent());
    }

    /**
     * Quét sâu dự án cục bộ từ thư mục rootPath, đọc nội dung các file config để nhận diện chi tiết
     */
    public ProjectConfig analyzeLocalProject(Path rootPath) {
        try {
            List<String> filePaths = localScanner.scanLocalDirectory(rootPath, 4);
            ProjectConfig config = analyzeFilePaths(filePaths);

            // 1. Tên App từ thư mục gốc
            String folderName = rootPath.getFileName() != null ? rootPath.getFileName().toString() : "my-app";
            config.setAppName(folderName.toLowerCase().replaceAll("[^a-z0-9_-]", "-"));

            Map<String, String> localContents = new HashMap<>();
            localScanner.readFileIfExists(rootPath, "pom.xml").ifPresent(c -> localContents.put("pom.xml", c));
            localScanner.readFileIfExists(rootPath, "build.gradle").ifPresent(c -> localContents.put("build.gradle", c));
            localScanner.readFileIfExists(rootPath, "build.gradle.kts").ifPresent(c -> localContents.put("build.gradle.kts", c));
            localScanner.readFileIfExists(rootPath, "src/main/resources/application.yml").ifPresent(c -> localContents.put("src/main/resources/application.yml", c));
            localScanner.readFileIfExists(rootPath, "src/main/resources/application.yaml").ifPresent(c -> localContents.put("src/main/resources/application.yaml", c));
            localScanner.readFileIfExists(rootPath, "src/main/resources/application.properties").ifPresent(c -> localContents.put("src/main/resources/application.properties", c));
            localScanner.readFileIfExists(rootPath, "package.json").ifPresent(c -> localContents.put("package.json", c));
            localScanner.readFileIfExists(rootPath, "requirements.txt").ifPresent(c -> localContents.put("requirements.txt", c));

            enrichWithFileContents(config, localContents);

            // Kiểm tra .env hoặc .env.example cho port bổ sung
            Optional<String> envContent = localScanner.readFileIfExists(rootPath, ".env");
            if (!envContent.isPresent()) envContent = localScanner.readFileIfExists(rootPath, ".env.example");
            if (envContent.isPresent()) {
                Integer port = parser.parseServerPort(envContent.get());
                if (port != null) {
                    config.setAppPort(port);
                    config.setHostPort(port);
                }
            }

            return config;
        } catch (Exception e) {
            return analyzeFilePaths(List.of());
        }
    }
}
