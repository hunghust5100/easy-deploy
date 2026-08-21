# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho Golang (Go)
# Tĩnh hóa Binary (Static Build) & Tối ưu Dung lượng Siêu Nhẹ (~15MB)
# ==============================================================================

# 1. Base Image Builder (Go Alpine)
FROM golang:${config.techVersion?default("1.22")}-alpine AS builder

WORKDIR /app

# Cài đặt chứng chỉ SSL và build-base nếu cần
RUN apk add --no-cache git ca-certificates tzdata

# 2. COPY file khai báo dependencies (go.mod, go.sum)
COPY go.mod go.sum* ./
RUN go mod download

# 3. COPY toàn bộ mã nguồn và biên dịch Binary độc lập
COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -ldflags="-w -s" -o main .

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image Tối Giản (Alpine siêu nhẹ)
# ------------------------------------------------------------------------------
FROM alpine:3.19

WORKDIR /app

# Cài đặt SSL Certificates & Timezone data
RUN apk add --no-cache ca-certificates tzdata

# Tối ưu Bảo mật: Tạo và chuyển sang User thường (Non-root)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy Binary từ Builder
COPY --from=builder /app/main .

# 4. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 5. ENTRYPOINT chạy ứng dụng
ENTRYPOINT ["./main"]
