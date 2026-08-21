package com.easydeploy.core.model.enums;

public enum DeployMode {
    REMOTE_BUILD("🖥️ Remote Build trên VPS",
            "Mã nguồn được tải lên VPS và build Docker image trực tiếp tại VPS (Cần VPS RAM ≥ 2GB)."),
    REGISTRY_PULL("🐳 Docker Hub Registry Pull (Khuyên dùng)",
            "Đóng gói Image lên Docker Hub, VPS chỉ kéo Image về chạy (Siêu nhanh, bảo mật source code, tiết kiệm RAM cho VPS nhỏ 512MB-1GB).");

    private final String displayName;
    private final String description;

    DeployMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static DeployMode fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return REMOTE_BUILD;
        }
        for (DeployMode mode : DeployMode.values()) {
            if (mode.name().equalsIgnoreCase(text.trim())) {
                return mode;
            }
        }
        if ("remote_build".equalsIgnoreCase(text.trim())) return REMOTE_BUILD;
        if ("registry_pull".equalsIgnoreCase(text.trim())) return REGISTRY_PULL;

        return REMOTE_BUILD;
    }
}
