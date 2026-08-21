# ==============================================================================
# Multi-stage Dockerfile tối ưu cho Node.js Backend (Express/NestJS/Fastify)
# Tuân thủ quy trình 7 bước tiêu chuẩn & Non-root User
# ==============================================================================

# 1. Base Image Builder
FROM node:${config.techVersion?default("20")}-alpine AS builder

# 2. WORKDIR
WORKDIR /app

# 3. COPY package.json & lockfile (tùy chọn với *)
COPY package.json package-lock.json* yarn.lock* pnpm-lock.yaml* ./

# 4. RUN cài đặt toàn bộ dependencies (hỗ trợ npm ci, yarn, pnpm, fallback npm install)
RUN if [ -f package-lock.json ]; then \
      npm ci; \
    elif [ -f yarn.lock ]; then \
      yarn install --frozen-lockfile || yarn install; \
    elif [ -f pnpm-lock.yaml ]; then \
      npx pnpm install; \
    else \
      npm install; \
    fi

# 5. COPY mã nguồn & build (nếu có TypeScript / NestJS)
COPY . .
RUN npm run build --if-present 2>/dev/null || true

# ------------------------------------------------------------------------------
# Stage 2: Production Runner Image
# ------------------------------------------------------------------------------
FROM node:${config.techVersion?default("20")}-alpine
WORKDIR /app

# COPY package.json & chỉ cài đặt Production dependencies
COPY package.json package-lock.json* yarn.lock* pnpm-lock.yaml* ./
RUN if [ -f package-lock.json ]; then \
      npm ci --only=production; \
    elif [ -f yarn.lock ]; then \
      yarn install --production; \
    elif [ -f pnpm-lock.yaml ]; then \
      npx pnpm install --prod; \
    else \
      npm install --only=production; \
    fi && (npm cache clean --force 2>/dev/null || true)

# COPY toàn bộ mã nguồn hoặc dist từ Builder Stage
COPY --from=builder /app ./

# Tối ưu Bảo mật: Chạy bằng User 'node' có sẵn trong Alpine Image
USER node

# 6. EXPOSE
EXPOSE ${config.appPort?c}

# 7. CMD khởi chạy server (tự động phát hiện dist/main.js, index.js, app.js, server.js hoặc npm start)
CMD ["sh", "-c", "if [ -f dist/main.js ]; then node dist/main.js; elif [ -f dist/index.js ]; then node dist/index.js; elif [ -f index.js ]; then node index.js; elif [ -f server.js ]; then node server.js; elif [ -f app.js ]; then node app.js; elif [ -f src/index.js ]; then node src/index.js; else npm start; fi"]
