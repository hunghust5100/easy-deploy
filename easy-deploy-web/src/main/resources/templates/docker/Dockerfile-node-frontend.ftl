# ==============================================================================
# Multi-stage Dockerfile tối ưu cho Node.js Frontend (React/Vite/Vue)
# Phục vụ tĩnh bằng Nginx Alpine
# ==============================================================================

# Stage 1: Build
FROM node:${config.techVersion?default("20")}-alpine AS builder
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

COPY . .
RUN npm run build

# Chuẩn hóa output: gom dist/ hoặc build/ hoặc out/ vào /app/_static
RUN mkdir -p /app/_static && \
    if [ -d /app/dist ]; then cp -r /app/dist/* /app/_static/; \
    elif [ -d /app/build ]; then cp -r /app/build/* /app/_static/; \
    elif [ -d /app/out ]; then cp -r /app/out/* /app/_static/; \
    fi

# Stage 2: Nginx Alpine (~20MB)
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

RUN rm -rf ./*

COPY --from=builder /app/_static/ ./

EXPOSE ${config.appPort?c}

CMD ["nginx", "-g", "daemon off;"]
