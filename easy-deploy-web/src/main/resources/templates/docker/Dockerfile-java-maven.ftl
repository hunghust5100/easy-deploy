# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho Java Spring Boot (Maven)
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
# 1. Base Image Builder (JDK + Maven)
FROM maven:3.9.9-eclipse-temurin-${javaVer}-alpine AS builder

# 2. WORKDIR - Tạo và di chuyển vào thư mục làm việc
WORKDIR /app

# 3. COPY mã nguồn và file cấu hình
COPY . .

# 4. RUN biên dịch đóng gói JAR
RUN TARGET_MODULE="${config.appName!""}" && \
    if [ -n "$TARGET_MODULE" ] && [ -d "$TARGET_MODULE" ]; then \
        echo ">>> Monorepo detected. Building module: $TARGET_MODULE" && \
        mvn clean package -pl "$TARGET_MODULE" -am -DskipTests -B; \
    else \
        echo ">>> Standalone Maven detected. Building root project..." && \
        mvn clean package -DskipTests -B; \
    fi

# 5. Tự động trích xuất file JAR thực thi (loại trừ plain/sources/original JAR) vào /app/app.jar
RUN find . -path "*/target/*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" ! -name "original-*.jar" -type f -exec cp {} /app/app.jar \;

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image siêu nhẹ (JRE Alpine)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:${javaVer}-jre-alpine
WORKDIR /app

# Tối ưu Bảo mật: Tạo User thường (Non-root)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy file jar đã build chuẩn từ builder
COPY --from=builder --chown=appuser:appgroup /app/app.jar app.jar

USER appuser

# 6. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 7. ENTRYPOINT chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
