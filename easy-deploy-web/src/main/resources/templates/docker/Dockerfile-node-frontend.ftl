# ==============================================================================
# Multi-stage Dockerfile tối ưu cho Node.js Frontend (React/Vite/Vue)
# Tuân thủ quy trình 7 bước tiêu chuẩn & Phục vụ tĩnh bằng Nginx Alpine
# ==============================================================================

# 1. Base Image Builder (Node Alpine)
FROM node:${config.techVersion?default("20")}-alpine AS builder

# 2. WORKDIR
WORKDIR /app

# 3. COPY file khai báo thư viện và lockfiles (tùy chọn với *)
COPY package.json package-lock.json* yarn.lock* pnpm-lock.yaml* ./

# 4. RUN cài đặt dependencies linh hoạt (hỗ trợ npm ci, yarn, pnpm, fallback npm install)
RUN if [ -f package-lock.json ]; then \
      npm ci; \
    elif [ -f yarn.lock ]; then \
      yarn install --frozen-lockfile || yarn install; \
    elif [ -f pnpm-lock.yaml ]; then \
      npx pnpm install; \
    else \
      npm install; \
    fi

# 5. COPY mã nguồn còn lại và build ứng dụng Web
COPY . .
RUN npm run build

# ------------------------------------------------------------------------------
# Stage 2: Web Server Nginx Alpine nhẹ (~20MB)
# ------------------------------------------------------------------------------
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Xóa các file mặc định của Nginx
RUN rm -rf ./*

# Copy file tĩnh đã build từ Builder Stage (hỗ trợ dist/, build/, out/)
COPY --from=builder /app/dist ./ 2>/dev/null || true
RUN if [ ! -f index.html ]; then cp -r /app/build/* ./ 2>/dev/null || true; fi
RUN if [ ! -f index.html ]; then cp -r /app/out/* ./ 2>/dev/null || true; fi

# 6. EXPOSE cổng Web
EXPOSE ${config.appPort?c}

# 7. CMD chạy Nginx ngầm
CMD ["nginx", "-g", "daemon off;"]
