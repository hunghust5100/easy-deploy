#!/bin/bash
# ====================================================================
# Easy Deploy - Automated VPS Provisioning & Setup Script
# Application: ${appName}
# ====================================================================

set -e

echo -e "\e[34;1m[SETUP] Khởi tạo quy trình cấu hình tự động cho ${appName}...\e[0m"

<#if installDocker?? && installDocker>
# --------------------------------------------------------------------
# 1. Cài đặt Docker Engine & Docker Compose V2 Plugin
# --------------------------------------------------------------------
if ! command -v docker &> /dev/null; then
    echo -e "\e[36m[Step] Đang cài đặt Docker Engine...\e[0m"
    curl -fsSL https://get.docker.com | sh
else
    echo -e "\e[32m[OK] Docker Daemon đã được cài đặt trước đó.\e[0m"
fi

if ! docker compose version &> /dev/null; then
    echo -e "\e[36m[Step] Đang cài đặt Docker Compose Plugin...\e[0m"
    apt-get update && apt-get install -y docker-compose-plugin || true
fi
</#if>

<#if useDockerHub?? && useDockerHub>
# --------------------------------------------------------------------
# 2. Đăng nhập Docker Hub & Pull Image
# --------------------------------------------------------------------
<#if dockerHubUsername?? && dockerHubUsername != "" && dockerHubToken?? && dockerHubToken != "">
echo -e "\e[36m[Step] Đang đăng nhập Docker Hub (${dockerHubUsername})...\e[0m"
echo "${dockerHubToken}" | docker login -u "${dockerHubUsername}" --password-stdin
</#if>

<#if dockerImageTag?? && dockerImageTag != "">
echo -e "\e[36m[Step] Đang Pull Docker Image (${dockerImageTag})...\e[0m"
docker pull ${dockerImageTag}
</#if>
</#if>

<#if setupFirewall?? && setupFirewall>
# --------------------------------------------------------------------
# 3. Cấu hình Firewall UFW (Mở cổng 80, 443, 22)
# --------------------------------------------------------------------
if command -v ufw &> /dev/null; then
    echo -e "\e[36m[Step] Đang kích hoạt UFW Firewall và mở các cổng an toàn (80, 443, 22)...\e[0m"
    ufw allow 80/tcp || true
    ufw allow 443/tcp || true
    ufw allow 22/tcp || true
    ufw --force enable || true
fi
</#if>

<#if installNginx?? && installNginx>
# --------------------------------------------------------------------
# 4. Cài đặt & Cấu hình Nginx Host Reverse Proxy
# --------------------------------------------------------------------
if ! command -v nginx &> /dev/null; then
    echo -e "\e[36m[Step] Đang cài đặt Nginx trên Host VPS...\e[0m"
    apt-get update && apt-get install -y nginx
fi

if [ -f "nginx.conf" ]; then
    echo -e "\e[36m[Step] Đang nạp file cấu hình nginx.conf vào Host Nginx...\e[0m"
    cp nginx.conf /etc/nginx/sites-available/${appName}.conf
    ln -sf /etc/nginx/sites-available/${appName}.conf /etc/nginx/sites-enabled/
    nginx -t
    systemctl reload nginx
fi
</#if>

<#if installCertbot?? && installCertbot>
# --------------------------------------------------------------------
# 5. Cài đặt & Đăng ký Chứng chỉ SSL Let's Encrypt (Certbot)
# --------------------------------------------------------------------
if ! command -v certbot &> /dev/null; then
    echo -e "\e[36m[Step] Đang cài đặt Certbot & Plugin Nginx...\e[0m"
    apt-get update && apt-get install -y certbot python3-certbot-nginx
fi

<#if useSslipIo?? && useSslipIo>
# Tự động detect IP VPS và tạo domain sslip.io (không cần mua domain)
SSLIP_DOMAIN="$(curl -4s ifconfig.me).sslip.io"
echo -e "\e[36m[Step] Sử dụng sslip.io: Tên miền tự động = $SSLIP_DOMAIN\e[0m"

# Cập nhật nginx config với domain sslip.io
if [ -f "/etc/nginx/sites-available/${appName}.conf" ]; then
    sed -i "s/server_name .*/server_name $SSLIP_DOMAIN;/" /etc/nginx/sites-available/${appName}.conf
    nginx -t && systemctl reload nginx
fi

certbot --nginx -d "$SSLIP_DOMAIN" --non-interactive --agree-tos -m ${adminEmail!"admin@example.com"} || echo -e "\e[33m[Warning] Không thể cấp phát SSL cho $SSLIP_DOMAIN. Certbot rate-limit hoặc lỗi mạng.\e[0m"
<#else>
<#if domainName?? && domainName != "" && domainName != "localhost">
echo -e "\e[36m[Step] Đang đăng ký chứng chỉ HTTPS Let's Encrypt cho ${domainName}...\e[0m"
certbot --nginx -d ${domainName} --non-interactive --agree-tos -m ${adminEmail!"admin@example.com"} || echo -e "\e[33m[Warning] Không thể cấp phát SSL tự động cho ${domainName}. Hãy đảm bảo Domain đã trỏ A Record về IP VPS.\e[0m"
</#if>
</#if>
</#if>

echo -e "\e[32;1m[SUCCESS] Hoàn tất quá trình Setup Server!\e[0m"

