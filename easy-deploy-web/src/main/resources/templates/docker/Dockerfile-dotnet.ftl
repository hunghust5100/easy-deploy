# ==============================================================================
# Multi-stage Dockerfile chuẩn tối ưu cho .NET / C# (ASP.NET Core)
# Tối ưu kích thước với .NET Alpine Runtime & Non-root User
# ==============================================================================

# 1. Base Image Builder (Dotnet SDK)
FROM mcr.microsoft.com/dotnet/sdk:${config.techVersion?default("8.0")}-alpine AS builder

WORKDIR /src

# Copy project files & restore dependencies trước để cache layer
COPY *.csproj ./
RUN dotnet restore 2>/dev/null || true

# Copy toàn bộ mã nguồn và Publish Release
COPY . .
RUN dotnet publish -c Release -o /app/publish /p:UseAppHost=false

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image Tối Giản (ASP.NET Alpine)
# ------------------------------------------------------------------------------
FROM mcr.microsoft.com/dotnet/aspnet:${config.techVersion?default("8.0")}-alpine

WORKDIR /app

# Tối ưu Bảo mật: Chuyển sang User thường
USER $APP_UID

# Copy các tệp build đã publish
COPY --from=builder /app/publish .

# Biến môi trường lắng nghe Port
ENV ASPNETCORE_URLS=http://+:${config.appPort?c}
ENV DOTNET_RUNNING_IN_CONTAINER=true

# 2. EXPOSE cổng ứng dụng
EXPOSE ${config.appPort?c}

# 3. ENTRYPOINT khởi chạy (Tự động tìm DLL chính của ứng dụng)
ENTRYPOINT ["sh", "-c", "exec dotnet $(ls *.dll | grep -v 'System\\|Microsoft\\|Newtonsoft' | head -n 1)"]
