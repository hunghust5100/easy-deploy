# ==============================================================================
# Multi-stage Dockerfile tối ưu cho Node.js Backend (Express/NestJS)
# Tuân thủ quy trình 7 bước tiêu chuẩn & Non-root User
# ==============================================================================

# 1. Base Image Builder
FROM node:20-alpine AS builder

# 2. WORKDIR
WORKDIR /app

# 3. COPY package.json & lockfile
COPY package.json package-lock.json* ./

# 4. RUN cài đặt toàn bộ dependencies (bao gồm devDependencies để build)
RUN npm ci

# 5. COPY mã nguồn & build (nếu có TypeScript)
COPY . .
RUN npm run build --if-present

# ------------------------------------------------------------------------------
# Stage 2: Production Runner Image
# ------------------------------------------------------------------------------
FROM node:20-alpine
WORKDIR /app

# COPY package.json & chỉ cài đặt Production dependencies
COPY package.json package-lock.json* ./
RUN npm ci --only=production && npm cache clean --force

# COPY kết quả đã build từ Builder Stage
COPY --from=builder /app/dist ./dist

# Tối ưu Bảo mật: Chạy bằng User 'node' có sẵn trong Alpine Image
USER node

# 6. EXPOSE
EXPOSE ${config.appPort?c}

# 7. CMD khởi chạy server
CMD ["node", "dist/main.js"]
