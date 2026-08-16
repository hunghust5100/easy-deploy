# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho Java Spring Boot (Gradle)
# Tuân thủ quy trình 7 bước tiêu chuẩn & Tối ưu Non-root User
# ==============================================================================

# 1. Base Image Builder (JDK + Gradle Alpine)
FROM gradle:8.6-jdk${config.techVersion}-alpine AS builder

# 2. WORKDIR
WORKDIR /app

# 3. COPY mã nguồn và file cấu hình Gradle
COPY . .

# 4. RUN cấp quyền và biên dịch Boot JAR
RUN chmod +x ./gradlew 2>/dev/null || true
RUN if [ -f gradlew ]; then ./gradlew bootJar --no-daemon -x test; else gradle bootJar --no-daemon -x test; fi

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image siêu nhẹ (JRE Alpine)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:${config.techVersion}-jre-alpine
WORKDIR /app

# Tối ưu Bảo mật: Tạo và chuyển sang User thường (Non-root)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy file jar đã build
COPY --from=builder /app/build/libs/*.jar app.jar

# 6. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 7. ENTRYPOINT chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
