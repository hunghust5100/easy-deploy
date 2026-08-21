# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho PHP / Laravel / Symfony
# Tích hợp Composer, PHP-FPM, OPcache & Extensions
# ==============================================================================

# 1. Base Image Builder (Composer)
FROM composer:2 AS composer-stage

WORKDIR /app
COPY composer.json composer.lock* ./
RUN composer install --no-dev --no-scripts --no-autoloader --prefer-dist --ignore-platform-reqs

COPY . .
RUN composer dump-autoload --optimize --no-dev

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image (PHP-FPM Alpine)
# ------------------------------------------------------------------------------
FROM php:${config.techVersion?default("8.2")}-fpm-alpine

WORKDIR /var/www/html

# Cài đặt các gói hệ thống và thư viện mở rộng của PHP
RUN apk add --no-cache \
    curl \
    libpng-dev \
    libxml2-dev \
    libzip-dev \
    oniguruma-dev \
    postgresql-dev \
    zip \
    unzip

# Cài đặt PHP Extensions
RUN docker-php-ext-install \
    bcmath \
    ctype \
    fileinfo \
    mbstring \
    pdo \
    pdo_mysql \
    pdo_pgsql \
    opcache \
    xml \
    zip

# Copy mã nguồn và vendor từ Composer Stage
COPY --from=composer-stage /app /var/www/html

# Phân quyền cho www-data
RUN chown -R www-data:www-data /var/www/html/storage /var/www/html/bootstrap/cache 2>/dev/null || true

USER www-data

# 2. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 3. Chạy PHP Server
CMD ["php", "-S", "0.0.0.0:${config.appPort?c}", "-t", "public"]
