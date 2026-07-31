package com.easydeploy.core.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalTreeScanner {

    private static final Set<String> IGNORED_DIRS = Set.of(
        ".git", "node_modules", "build", "target", ".gradle", ".idea", ".vscode", "dist", "out"
    );

    public List<String> scanLocalDirectory(Path rootPath) throws IOException {
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Invalid directory path: " + rootPath);
        }

        try (Stream<Path> stream = Files.walk(rootPath, 4)) {
            return stream
                .filter(path -> !isIgnored(rootPath, path))
                .map(path -> rootPath.relativize(path).toString())
                .filter(pathStr -> !pathStr.isEmpty())
                .collect(Collectors.toList());
        }
    }

    private boolean isIgnored(Path rootPath, Path currentPath) {
        Path relativePath = rootPath.relativize(currentPath);
        for (Path part : relativePath) {
            if (IGNORED_DIRS.contains(part.toString())) {
                return true;
            }
        }
        return false;
    }
}
