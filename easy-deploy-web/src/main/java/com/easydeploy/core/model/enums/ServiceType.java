package com.easydeploy.core.model.enums;

public enum ServiceType {
    FRONTEND("Frontend Client"),
    BACKEND("Backend API / Service"),
    FULLSTACK("Fullstack Application"),
    DATABASE("Database Service"),
    REVERSE_PROXY("Reverse Proxy / Gateway"),
    WORKER("Background Worker");

    private final String description;

    ServiceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ServiceType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BACKEND;
        }
        for (ServiceType type : ServiceType.values()) {
            if (type.name().equalsIgnoreCase(text.trim())) {
                return type;
            }
        }
        return BACKEND;
    }
}
