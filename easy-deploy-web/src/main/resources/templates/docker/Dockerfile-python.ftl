# ==============================================================================
# Multi-stage Dockerfile tối ưu cho Python (FastAPI / Django / Flask)
# Tuân thủ quy trình 7 bước tiêu chuẩn & Base Image Slim & Non-root User
# ==============================================================================

# 1. Base Image Builder (Python Slim)
FROM python:3.11-slim AS builder

# 2. WORKDIR
WORKDIR /app

# Ngăn Python tạo file .pyc và bật unbuffered output
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# 3. COPY file khai báo thư viện
COPY requirements.txt .

# 4. RUN cài đặt thư viện vào thư mục wheels/user
RUN pip install --no-cache-dir --user -r requirements.txt

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image gọn nhẹ
# ------------------------------------------------------------------------------
FROM python:3.11-slim
WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PATH=/root/.local/bin:$PATH

# Tối ưu Bảo mật: Tạo Non-root User
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Copy installed packages từ Builder Stage
COPY --from=builder /root/.local /home/appuser/.local
ENV PATH=/home/appuser/.local/bin:$PATH

# 5. COPY mã nguồn còn lại
COPY . .

# Phân quyền cho Non-root User
RUN chown -R appuser:appgroup /app
USER appuser

# 6. EXPOSE
EXPOSE ${config.appPort?c}

# 7. CMD chạy gunicorn / uvicorn
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "${config.appPort?c}"]
