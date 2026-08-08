# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho Java Spring Boot (Gradle)
# Tuân thủ quy trình 7 bước tiêu chuẩn & Tối ưu Layered JAR & Non-root User
# ==============================================================================

# 1. Base Image Builder (JDK + Gradle Alpine)
FROM gradle:8.6-jdk${config.techVersion}-alpine AS builder

# 2. WORKDIR
WORKDIR /app

# 3. COPY file khai báo thư viện trước
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# 4. RUN tải dependencies trước để cache layer
RUN ./gradlew dependencies --no-daemon || true

# 5. COPY mã nguồn và đóng gói JAR
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Tách Spring Boot Layered JAR
RUN java -Djarmode=layertools -jar build/libs/*.jar extract || java -jar build/libs/*.jar extract --destination .

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image (JRE Alpine)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:${config.techVersion}-jre-alpine
WORKDIR /app

# Non-root User
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# 6. EXPOSE
EXPOSE ${config.appPort?c}

# 7. ENTRYPOINT
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
