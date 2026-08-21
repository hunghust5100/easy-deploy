# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho Java Spring Boot (Gradle)
# Hỗ trợ cả Single-module và Multi-module (Monorepo), tối ưu Non-root User
# ==============================================================================

<#assign rawVer = (config.techVersion!"21")?trim>
<#if rawVer == "8" || rawVer == "1.8">
  <#assign javaVer = "8">
<#elseif rawVer == "11" || rawVer == "11.0">
  <#assign javaVer = "11">
<#elseif rawVer == "17" || rawVer == "17.0">
  <#assign javaVer = "17">
<#else>
  <#assign javaVer = "21">
</#if>
# 1. Base Image Builder (JDK + Gradle Alpine)
FROM gradle:8.14-jdk${javaVer}-alpine AS builder

# 2. WORKDIR
WORKDIR /app

# 3. COPY toàn bộ mã nguồn và cấu hình Gradle
COPY . .

# 4. RUN cấp quyền và tự động khôi phục Gradle Wrapper nếu thiếu
RUN chmod +x ./gradlew 2>/dev/null || true
RUN if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then gradle wrapper 2>/dev/null || true; fi

# Tự động phát hiện Multi-module hoặc Single-module:
# - Ưu tiên gradlew wrapper theo đúng phiên bản của repo, fallback về gradle hệ thống nếu cần
# - Tự động scan tìm module Spring Boot trong Multi-module project
RUN if [ -f gradlew ] && [ -f gradle/wrapper/gradle-wrapper.jar ]; then BUILD_CMD="./gradlew"; else BUILD_CMD="gradle"; fi && \
    if [ -f settings.gradle ] || [ -f settings.gradle.kts ]; then \
      TARGET_MODULE="${config.appName}"; \
      if [ -d "$TARGET_MODULE" ] && ( [ -f "$TARGET_MODULE/build.gradle" ] || [ -f "$TARGET_MODULE/build.gradle.kts" ] ); then \
        echo ">>> [EasyDeploy] Monorepo Gradle. Tien hanh build :$TARGET_MODULE:bootJar..." && \
        $BUILD_CMD ":$TARGET_MODULE:bootJar" --no-daemon -x test || $BUILD_CMD ":$TARGET_MODULE:build" --no-daemon -x test; \
      else \
        BOOT_MODULE=$(grep -rl "org.springframework.boot" */build.gradle* 2>/dev/null | head -1 | xargs -I{} dirname {} 2>/dev/null) && \
        if [ -n "$BOOT_MODULE" ]; then \
          echo ">>> [EasyDeploy] Multi-module Gradle phat hien. Tien hanh build :$BOOT_MODULE:bootJar..." && \
          $BUILD_CMD ":$BOOT_MODULE:bootJar" --no-daemon -x test; \
        else \
          echo ">>> [EasyDeploy] Single/Standard Gradle. Tien hanh build bootJar..." && \
          $BUILD_CMD bootJar --no-daemon -x test || $BUILD_CMD build -x test; \
        fi; \
      fi; \
    else \
      SUB_DIR=$(find . -maxdepth 2 -name "settings.gradle*" | head -1 | xargs -I{} dirname {} 2>/dev/null); \
      if [ -n "$SUB_DIR" ] && [ "$SUB_DIR" != "." ]; then \
        echo ">>> [EasyDeploy] Found standalone Gradle project in $SUB_DIR. CD into it..." && \
        cd "$SUB_DIR" && \
        if [ -f gradlew ]; then SUB_BUILD_CMD="./gradlew"; else SUB_BUILD_CMD="gradle"; fi && \
        chmod +x ./gradlew 2>/dev/null || true && \
        $SUB_BUILD_CMD bootJar --no-daemon -x test || $SUB_BUILD_CMD build -x test; \
      else \
        echo ">>> [EasyDeploy] Single Gradle. Tien hanh build bootJar..." && \
        $BUILD_CMD bootJar --no-daemon -x test || $BUILD_CMD build -x test; \
      fi; \
    fi

# 5. Tự động trích xuất file JAR thực thi (loại trừ plain/sources JAR) vào vị trí chuẩn /app/app.jar
RUN if [ -d "${config.appName}/build/libs" ]; then \
      find "${config.appName}/build/libs" -name "*.jar" ! -name "*-plain.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" -type f -exec cp {} /app/app.jar \; ; \
    fi || true; \
    if [ ! -f /app/app.jar ]; then \
      find . -path "*/build/libs/*.jar" ! -name "*-plain.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" -type f -exec cp {} /app/app.jar \; ; \
    fi || true; \
    if [ ! -f /app/app.jar ]; then \
      find . -name "*.jar" ! -name "*-plain.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" -type f -exec cp {} /app/app.jar \; ; \
    fi

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image siêu nhẹ (JRE Alpine)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:${javaVer}-jre-alpine
WORKDIR /app

# Tối ưu Bảo mật: Tạo User thường (Non-root)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy file jar đã được trích xuất từ stage builder
COPY --from=builder --chown=appuser:appgroup /app/app.jar app.jar

USER appuser

# 6. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 7. ENTRYPOINT chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
