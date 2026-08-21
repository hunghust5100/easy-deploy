# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho Rust
# Biên dịch Release Mode & Tối ưu Dung lượng Runtime
# ==============================================================================

# 1. Base Image Builder (Rust Debian Slim)
FROM rust:${config.techVersion?default("1.78")}-slim AS builder

WORKDIR /app

# Cài đặt công cụ build cần thiết
RUN apt-get update && apt-get install -y pkg-config libssl-dev && rm -rf /var/lib/apt/lists/*

# 2. COPY file khai báo Cargo
COPY Cargo.toml Cargo.lock* ./

# 3. Tạo dummy main.rs để cache dependencies trước
RUN mkdir src && echo "fn main() {}" > src/main.rs
RUN cargo build --release
RUN rm -rf src

# 4. COPY mã nguồn thực tế và biên dịch Release
COPY . .
RUN touch src/main.rs && cargo build --release

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image Tối Giản
# ------------------------------------------------------------------------------
FROM debian:bookworm-slim

WORKDIR /app

RUN apt-get update && apt-get install -y ca-certificates libssl3 tzdata && rm -rf /var/lib/apt/lists/*

# Tối ưu Bảo mật: Tạo và chuyển sang User thường (Non-root)
RUN useradd -m -u 1001 -s /bin/bash appuser
USER appuser

# Copy Binary từ Builder
COPY --from=builder /app/target/release/${config.appName} /app/main

# 5. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 6. ENTRYPOINT chạy ứng dụng
ENTRYPOINT ["./main"]
