# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho Java Spring Boot (Maven)
# Tuân thủ quy trình 7 bước tiêu chuẩn & Tối ưu Non-root User
# ==============================================================================

# 1. Base Image Builder (JDK + Maven)
FROM maven:3.9.6-eclipse-temurin-${config.techVersion}-alpine AS builder

# 2. WORKDIR - Tạo và di chuyển vào thư mục làm việc
WORKDIR /app

# 3. COPY mã nguồn và file cấu hình
COPY . .

# 4. RUN biên dịch đóng gói JAR
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image siêu nhẹ (JRE Alpine)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:${config.techVersion}-jre-alpine
WORKDIR /app

# Tối ưu Bảo mật: Tạo và chuyển sang User thường (Non-root)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy file jar đã build
COPY --from=builder /app/target/*.jar app.jar

# 6. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 7. ENTRYPOINT chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
