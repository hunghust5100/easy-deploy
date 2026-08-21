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
FROM maven:3.9.6-eclipse-temurin-${javaVer}-alpine AS builder

# 2. WORKDIR - Tạo và di chuyển vào thư mục làm việc
WORKDIR /app

# 3. COPY mã nguồn và file cấu hình
COPY . .

# 4. RUN biên dịch đóng gói JAR
RUN if [ -f pom.xml ]; then \
      mvn clean package -DskipTests -B; \
    else \
      SUB_DIR=$(find . -maxdepth 2 -name "pom.xml" | head -1 | xargs -I{} dirname {} 2>/dev/null); \
      if [ -n "$SUB_DIR" ] && [ "$SUB_DIR" != "." ]; then \
        echo ">>> [EasyDeploy] Found standalone Maven project in $SUB_DIR. CD into it..." && \
        cd "$SUB_DIR" && \
        mvn clean package -DskipTests -B; \
      else \
        echo ">>> [EasyDeploy] pom.xml not found. Trying to run mvn anyway..." && \
        mvn clean package -DskipTests -B; \
      fi; \
    fi

# 5. Tự động trích xuất file JAR thực thi (loại trừ plain/sources/original JAR) vào /app/app.jar
RUN find . -path "*/target/*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" ! -name "original-*.jar" -type f -exec cp {} /app/app.jar \;

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image siêu nhẹ (JRE Alpine)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:${javaVer}-jre-alpine
WORKDIR /app

# Tối ưu Bảo mật: Tạo và chuyển sang User thường (Non-root)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy file jar đã build chuẩn từ builder
COPY --from=builder /app/app.jar app.jar

# 6. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 7. ENTRYPOINT chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
