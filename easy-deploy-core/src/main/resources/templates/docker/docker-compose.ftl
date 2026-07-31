version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ${config.appName}_app
    restart: always
    environment:
<#if config.dbType == "POSTGRESQL">
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/${config.dbName}
      - SPRING_DATASOURCE_USERNAME=${config.dbUser}
      - SPRING_DATASOURCE_PASSWORD=${config.dbPass}
<#elseif config.dbType == "MYSQL">
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/${config.dbName}
      - SPRING_DATASOURCE_USERNAME=${config.dbUser}
      - SPRING_DATASOURCE_PASSWORD=${config.dbPass}
</#if>
<#if config.dbType != "NONE">
    depends_on:
      db:
        condition: service_healthy
</#if>
    networks:
      - app-network

<#if config.dbType == "POSTGRESQL">
  db:
    image: postgres:16-alpine
    container_name: ${config.appName}_db
    restart: always
    environment:
      POSTGRES_DB: ${config.dbName}
      POSTGRES_USER: ${config.dbUser}
      POSTGRES_PASSWORD: ${config.dbPass}
    ports:
      - "${config.dbPort?c}:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${config.dbUser}"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - app-network
</#if>

<#if config.enableNginx>
  nginx:
    image: nginx:alpine
    container_name: ${config.appName}_nginx
    restart: always
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
    depends_on:
      - app
    networks:
      - app-network
</#if>

networks:
  app-network:
    driver: bridge

<#if config.dbType == "POSTGRESQL">
volumes:
  pgdata:
</#if>
