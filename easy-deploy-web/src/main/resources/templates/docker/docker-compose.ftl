
services:
<#if (config.services?? && config.services?size > 1)>
<#list config.services as service>
<#if service.enabled>
  ${service.name}:
<#if (config.deployMode?? && (config.deployMode == "registry_pull" || config.deployMode == "REGISTRY_PULL" || config.deployMode?upper_case == "REGISTRY_PULL")) && config.dockerHubUsername?? && config.dockerHubUsername != "">
    image: ${config.dockerHubUsername}/${config.appName}-${service.name}:${(config.dockerImageTag?? && config.dockerImageTag != "")?then(config.dockerImageTag, "latest")}
<#else>
    build:
<#if (service.techStack == "JAVA_GRADLE" || service.techStack == "JAVA_MAVEN") && service.relativePath != ".">
      context: .
      dockerfile: ${service.relativePath}/Dockerfile
<#else>
      context: ${service.relativePath}
      dockerfile: Dockerfile
</#if>
</#if>
    container_name: ${config.appName}_${service.name}
    restart: always
<#if service.backend && config.dbType != "NONE">
    environment:
<#if config.dbType == "POSTGRESQL">
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/${config.dbName}
      - SPRING_DATASOURCE_USERNAME=${config.dbUser}
      - SPRING_DATASOURCE_PASSWORD=${config.dbPass}
<#elseif config.dbType == "MYSQL" || config.dbType == "MARIADB">
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/${config.dbName}
      - SPRING_DATASOURCE_USERNAME=${config.dbUser}
      - SPRING_DATASOURCE_PASSWORD=${config.dbPass}
<#elseif config.dbType == "MONGODB">
      - MONGO_URI=mongodb://${config.dbUser}:${config.dbPass}@db:27017/${config.dbName}?authSource=admin
<#elseif config.dbType == "REDIS">
      - REDIS_HOST=db
      - REDIS_PORT=6379
</#if>
<#if service.envVars?? && (service.envVars?size > 0)>
<#list service.envVars?keys as key>
      - ${key}=${service.envVars[key]}
</#list>
</#if>
<#if config.envVars?? && (config.envVars?size > 0)>
<#list config.envVars?keys as key>
      - ${key}=${config.envVars[key]}
</#list>
</#if>
    depends_on:
      db:
        condition: service_healthy
<#else>
<#if (service.envVars?? && (service.envVars?size > 0)) || (config.envVars?? && (config.envVars?size > 0))>
    environment:
<#if service.envVars?? && (service.envVars?size > 0)>
<#list service.envVars?keys as key>
      - ${key}=${service.envVars[key]}
</#list>
</#if>
<#if config.envVars?? && (config.envVars?size > 0)>
<#list config.envVars?keys as key>
      - ${key}=${config.envVars[key]}
</#list>
</#if>
</#if>
</#if>
    ports:
      - "${service.hostPort?c}:${service.containerPort?c}"
    networks:
      - app-network

</#if>
</#list>
<#else>
  app:
<#if (config.deployMode?? && (config.deployMode == "registry_pull" || config.deployMode == "REGISTRY_PULL" || config.deployMode?upper_case == "REGISTRY_PULL")) && config.dockerHubUsername?? && config.dockerHubUsername != "">
    image: ${config.dockerHubUsername}/${config.appName}:${(config.dockerImageTag?? && config.dockerImageTag != "")?then(config.dockerImageTag, "latest")}
<#else>
    build:
      context: .
      dockerfile: Dockerfile
</#if>
    container_name: ${config.appName}_app
    restart: always
<#if config.dbType != "NONE">
    environment:
<#if config.dbType == "POSTGRESQL">
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/${config.dbName}
      - SPRING_DATASOURCE_USERNAME=${config.dbUser}
      - SPRING_DATASOURCE_PASSWORD=${config.dbPass}
<#elseif config.dbType == "MYSQL" || config.dbType == "MARIADB">
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/${config.dbName}
      - SPRING_DATASOURCE_USERNAME=${config.dbUser}
      - SPRING_DATASOURCE_PASSWORD=${config.dbPass}
<#elseif config.dbType == "MONGODB">
      - MONGO_URI=mongodb://${config.dbUser}:${config.dbPass}@db:27017/${config.dbName}?authSource=admin
<#elseif config.dbType == "REDIS">
      - REDIS_HOST=db
      - REDIS_PORT=6379
</#if>
<#if config.envVars?? && (config.envVars?size > 0)>
<#list config.envVars?keys as key>
      - ${key}=${config.envVars[key]}
</#list>
</#if>
    depends_on:
      db:
        condition: service_healthy
<#else>
<#if config.envVars?? && (config.envVars?size > 0)>
    environment:
<#list config.envVars?keys as key>
      - ${key}=${config.envVars[key]}
</#list>
</#if>
</#if>
<#if !config.enableNginx || config.hostPort != 80>
    ports:
      - "${config.hostPort?c}:${config.appPort?c}"
</#if>
    networks:
      - app-network
</#if>

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

<#elseif config.dbType == "MYSQL">
  db:
    image: mysql:8.0
    container_name: ${config.appName}_db
    restart: always
    environment:
      MYSQL_DATABASE: ${config.dbName}
      MYSQL_USER: ${config.dbUser}
      MYSQL_PASSWORD: ${config.dbPass}
      MYSQL_ROOT_PASSWORD: ${config.dbPass}
    ports:
      - "${config.dbPort?c}:3306"
    volumes:
      - mydata:/var/lib/mysql
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h localhost -u root -p${config.dbPass}"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - app-network

<#elseif config.dbType == "MARIADB">
  db:
    image: mariadb:11-alpine
    container_name: ${config.appName}_db
    restart: always
    environment:
      MARIADB_DATABASE: ${config.dbName}
      MARIADB_USER: ${config.dbUser}
      MARIADB_PASSWORD: ${config.dbPass}
      MARIADB_ROOT_PASSWORD: ${config.dbPass}
    ports:
      - "${config.dbPort?c}:3306"
    volumes:
      - mydata:/var/lib/mysql
    healthcheck:
      test: ["CMD-SHELL", "healthcheck.sh --connect --innodb_initialized"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - app-network

<#elseif config.dbType == "MONGODB">
  db:
    image: mongo:7-jammy
    container_name: ${config.appName}_db
    restart: always
    environment:
      MONGO_INITDB_ROOT_USERNAME: ${config.dbUser}
      MONGO_INITDB_ROOT_PASSWORD: ${config.dbPass}
      MONGO_INITDB_DATABASE: ${config.dbName}
    ports:
      - "${config.dbPort?c}:27017"
    volumes:
      - mongodata:/data/db
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - app-network

<#elseif config.dbType == "REDIS">
  db:
    image: redis:7-alpine
    container_name: ${config.appName}_db
    restart: always
    ports:
      - "${config.dbPort?c}:6379"
    volumes:
      - redisdata:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
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
<#if (config.installNginx?? && config.installNginx)>
      - "${config.hostPort?c}:80"
<#else>
      - "80:80"
</#if>
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
    depends_on:
<#if (config.services?? && config.services?size > 1)>
<#list config.services as service>
<#if service.enabled>
      - ${service.name}
</#if>
</#list>
<#else>
      - app
</#if>
    networks:
      - app-network
</#if>

networks:
  app-network:
    driver: bridge

<#if config.dbType == "POSTGRESQL">
volumes:
  pgdata:
<#elseif config.dbType == "MYSQL" || config.dbType == "MARIADB">
volumes:
  mydata:
<#elseif config.dbType == "MONGODB">
volumes:
  mongodata:
<#elseif config.dbType == "REDIS">
volumes:
  redisdata:
</#if>
