# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho Next.js (SSR / Fullstack Standalone)
# Tối ưu kích thước với Next.js Standalone Mode & Non-root User
# ==============================================================================

# Stage 1: Dependencies
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

# Stage 2: Builder
FROM node:${config.techVersion?default("20")}-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# Chuẩn hóa output: nếu có standalone thì dùng standalone, không thì giữ nguyên .next
RUN mkdir -p /app/_output && \
    if [ -d /app/.next/standalone ]; then \
      cp -r /app/.next/standalone/* /app/_output/; \
      mkdir -p /app/_output/.next/static && \
      cp -r /app/.next/static/* /app/_output/.next/static/ 2>/dev/null || true; \
    else \
      cp -r /app/node_modules /app/_output/node_modules 2>/dev/null || true; \
      cp -r /app/.next /app/_output/.next; \
      cp /app/package.json /app/_output/package.json; \
    fi && \
    if [ -d /app/public ]; then cp -r /app/public /app/_output/public; fi

# Stage 3: Runner (~80MB)
FROM node:${config.techVersion?default("20")}-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=${config.appPort?c}
ENV HOSTNAME="0.0.0.0"

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder --chown=nextjs:nodejs /app/_output ./

USER nextjs

EXPOSE ${config.appPort?c}

CMD ["sh", "-c", "if [ -f server.js ]; then node server.js; else npm start; fi"]
