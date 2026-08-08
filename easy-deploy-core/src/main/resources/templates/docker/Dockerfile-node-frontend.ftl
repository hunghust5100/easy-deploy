# ==============================================================================
# Multi-stage Dockerfile tối ưu cho Node.js Frontend (React/Vite/Vue)
# Tuân thủ quy trình 7 bước tiêu chuẩn & Phục vụ tĩnh bằng Nginx Alpine
# ==============================================================================

# 1. Base Image Builder (Node Alpine)
FROM node:20-alpine AS builder

# 2. WORKDIR
WORKDIR /app

# 3. COPY file khai báo thư viện
COPY package.json package-lock.json* ./

# 4. RUN cài đặt thư viện sạch (npm ci)
RUN npm ci

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

# Copy file tĩnh đã build từ Builder Stage
COPY --from=builder /app/dist ./

# 6. EXPOSE cổng Web
EXPOSE ${config.appPort?c}

# 7. CMD chạy Nginx ngầm
CMD ["nginx", "-g", "daemon off;"]
