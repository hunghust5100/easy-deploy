package com.easydeploy.core.model.enums;

public enum DbType {
    POSTGRESQL("🐘 PostgreSQL", 5432),
    MYSQL("🐬 MySQL 8.0", 3306),
    MARIADB("🦭 MariaDB", 3306),
    MONGODB("🍃 MongoDB", 27017),
    REDIS("⚡ Redis Cache", 6379),
    NONE("🚫 Không dùng Database", 0);

    private final String displayName;
    private final int defaultPort;

    DbType(String displayName, int defaultPort) {
        this.displayName = displayName;
        this.defaultPort = defaultPort;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public static DbType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return POSTGRESQL;
        }
        for (DbType type : DbType.values()) {
            if (type.name().equalsIgnoreCase(text.trim())) {
                return type;
            }
        }
        return POSTGRESQL;
    }
}
