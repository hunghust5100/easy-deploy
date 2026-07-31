# Multi-stage Dockerfile for Java Spring Boot (Maven)
FROM maven:3.9.6-eclipse-temurin-${config.techVersion}-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:${config.techVersion}-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=builder /app/target/*.jar app.jar
EXPOSE ${config.appPort?c}
ENTRYPOINT ["java", "-jar", "app.jar"]
