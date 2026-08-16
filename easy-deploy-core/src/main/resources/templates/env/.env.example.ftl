# ─────────────────────────────────────────────────────────────
# 🚀 Easy Deploy Environment Configuration Template (.env.example)
# Auto-generated for ${config.appName}
# ─────────────────────────────────────────────────────────────

# Application Ports & Server Configuration
APP_NAME=${config.appName}
APP_ENV=production
APP_PORT=${config.appPort?c}
SERVER_PORT=${config.appPort?c}
PORT=${config.appPort?c}
HOST_PORT=${config.hostPort?c}
DOMAIN_NAME=${config.domainName}

<#if config.dbType != "NONE">
# Database Configuration (${config.dbType})
DB_TYPE=${config.dbType}
DB_HOST=db
DB_PORT=${config.dbPort?c}
DB_NAME=${config.dbName}
DB_DATABASE=${config.dbName}
DB_USER=${config.dbUser}
DB_USERNAME=${config.dbUser}
DB_PASS=${config.dbPass}
DB_PASSWORD=${config.dbPass}

<#if config.dbType == "POSTGRESQL">
POSTGRES_DB=${config.dbName}
POSTGRES_USER=${config.dbUser}
POSTGRES_PASSWORD=${config.dbPass}
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/${config.dbName}
SPRING_DATASOURCE_USERNAME=${config.dbUser}
SPRING_DATASOURCE_PASSWORD=${config.dbPass}
<#elseif config.dbType == "MYSQL" || config.dbType == "MARIADB">
MYSQL_DATABASE=${config.dbName}
MYSQL_USER=${config.dbUser}
MYSQL_PASSWORD=${config.dbPass}
MYSQL_ROOT_PASSWORD=${config.dbPass}
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/${config.dbName}
SPRING_DATASOURCE_USERNAME=${config.dbUser}
SPRING_DATASOURCE_PASSWORD=${config.dbPass}
<#elseif config.dbType == "MONGODB">
MONGO_INITDB_DATABASE=${config.dbName}
MONGO_INITDB_ROOT_USERNAME=${config.dbUser}
MONGO_INITDB_ROOT_PASSWORD=${config.dbPass}
MONGO_URI=mongodb://${config.dbUser}:${config.dbPass}@db:27017/${config.dbName}?authSource=admin
<#elseif config.dbType == "REDIS">
REDIS_HOST=db
REDIS_PORT=6379
</#if>
</#if>

<#if config.envVars?? && (config.envVars?size > 0)>
# Custom Project Variables
<#list config.envVars?keys as key>
${key}=${config.envVars[key]}
</#list>
</#if>
