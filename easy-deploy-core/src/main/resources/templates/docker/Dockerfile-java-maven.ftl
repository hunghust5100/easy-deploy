# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho Java Spring Boot (Maven)
# Tuân thủ quy trình 7 bước tiêu chuẩn & Tối ưu Layered JAR & Non-root User
# ==============================================================================

# 1. Base Image Builder (JDK + Maven)
FROM maven:3.9.6-eclipse-temurin-${config.techVersion}-alpine AS builder

# 2. WORKDIR - Tạo và di chuyển vào thư mục làm việc
WORKDIR /app

# 3. COPY file khai báo thư viện để cache layer dependencies
COPY pom.xml .

# 4. RUN cài đặt thư viện trước (Docker Cache Layer)
RUN mvn dependency:go-offline -B

# 5. COPY mã nguồn còn lại và biên dịch
COPY src ./src
RUN mvn package -DskipTests

# Tách Spring Boot Layered JAR thành 4 phần (dependencies, loader, snapshot, application)
RUN java -Djarmode=layertools -jar target/*.jar extract || java -jar target/*.jar extract --destination .

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image siêu nhẹ (JRE Alpine)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:${config.techVersion}-jre-alpine
WORKDIR /app

# Tối ưu Bảo mật: Tạo và chuyển sang User thường (Non-root)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy 4 layers của Spring Boot Layered JAR
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# 6. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 7. ENTRYPOINT chạy ứng dụng
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
