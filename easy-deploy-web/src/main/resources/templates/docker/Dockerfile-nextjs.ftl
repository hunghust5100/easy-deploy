# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho Next.js (SSR / Fullstack Standalone)
# Tối ưu kích thước với Next.js Standalone Mode & Non-root User
# ==============================================================================

# 1. Base Image Dependencies
FROM node:${config.techVersion?default("20")}-alpine AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app

COPY package.json package-lock.json* yarn.lock* pnpm-lock.yaml* ./
RUN if [ -f package-lock.json ]; then \
      npm ci; \
    elif [ -f yarn.lock ]; then \
      yarn install --frozen-lockfile || yarn install; \
    elif [ -f pnpm-lock.yaml ]; then \
      npx pnpm install; \
    else \
      npm install; \
    fi

# ------------------------------------------------------------------------------
# Stage 2: Builder
# ------------------------------------------------------------------------------
FROM node:${config.techVersion?default("20")}-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Tắt telemetry trong quá trình build
ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# ------------------------------------------------------------------------------
# Stage 3: Runner (Production Runtime Image ~80MB)
# ------------------------------------------------------------------------------
FROM node:${config.techVersion?default("20")}-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=${config.appPort?c}
ENV HOSTNAME="0.0.0.0"

# Tối ưu Bảo mật: Tạo và chuyển sang User thường
RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

# Copy Static files & Standalone Output
COPY --from=builder /app/public ./public 2>/dev/null || true
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./ 2>/dev/null || COPY --from=builder --chown=nextjs:nodejs /app/.next ./.next
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static 2>/dev/null || true

USER nextjs

# 2. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 3. Khởi chạy Next.js Standalone Server hoặc npm start
CMD ["sh", "-c", "if [ -f server.js ]; then node server.js; else npm start; fi"]
