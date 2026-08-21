# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho Ruby on Rails
# Tích hợp Bundler, Bootsnap & Puma Server
# ==============================================================================

# 1. Base Image Builder (Ruby Alpine)
FROM ruby:${config.techVersion?default("3.3")}-alpine AS builder

WORKDIR /app

# Cài đặt công cụ build cần thiết
RUN apk add --no-cache build-base git tzdata postgresql-dev yaml-dev

# Cài đặt Gems
COPY Gemfile Gemfile.lock* ./
RUN bundle config set --local without 'development test' && \
    bundle install --jobs=4 --retry=3

# Copy mã nguồn
COPY . .

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image Tối Giản
# ------------------------------------------------------------------------------
FROM ruby:${config.techVersion?default("3.3")}-alpine

WORKDIR /app

RUN apk add --no-cache tzdata postgresql-client yaml

# Tối ưu Bảo mật: Tạo và chuyển sang User thường
RUN addgroup -S appgroup && adduser -S rails -G appgroup
USER rails

# Copy Gems và Mã nguồn từ Builder
COPY --from=builder /usr/local/bundle /usr/local/bundle
COPY --from=builder --chown=rails:appgroup /app /app

ENV RAILS_ENV=production
ENV RAILS_SERVE_STATIC_FILES=true
ENV RAILS_LOG_TO_STDOUT=true
ENV PORT=${config.appPort?c}

# 2. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 3. Khởi chạy Rails Puma Server
CMD ["bin/rails", "server", "-b", "0.0.0.0", "-p", "${config.appPort?c}"]
