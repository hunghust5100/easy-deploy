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
echo "${dockerHubToken}" | docker login -u "${dockerHubUsername}" --password-stdin || echo -e "\e[33m[Warning] Đăng nhập Docker Hub thất bại hoặc không cần thiết, tiếp tục...\e[0m"
</#if>

<#if dockerImageTag?? && dockerImageTag != "">
echo -e "\e[36m[Step] Đang Pull Docker Image (${dockerImageTag})...\e[0m"
docker pull ${dockerImageTag} || echo -e "\e[33m[Warning] Không thể pull image ${dockerImageTag}, sẽ tự động build từ source...\e[0m"
</#if>
</#if>

<#if setupFirewall?? && setupFirewall>
# --------------------------------------------------------------------
# 3. Cấu hình Firewall UFW (Mở cổng 80, 443, 22)
# --------------------------------------------------------------------
if command -v ufw &> /dev/null; then
    echo -e "\e[36m[Step] Đang kích hoạt UFW Firewall và mở các cổng an toàn (80, 443, 22, ${hostPort?c})...\e[0m"
    ufw allow 80/tcp || true
    ufw allow 443/tcp || true
    ufw allow 22/tcp || true
    <#if hostPort != 80 && hostPort != 443 && hostPort != 22>
    ufw allow ${hostPort?c}/tcp || true
    </#if>
    ufw --force enable || true
fi
</#if>

<#if installNginx?? && installNginx>
# --------------------------------------------------------------------
# 4. Cài đặt & Cấu hình Nginx Host Reverse Proxy
# --------------------------------------------------------------------
if ! command -v nginx &> /dev/null; then
    echo -e "\e[36m[Step] Đang cài đặt Nginx trên Host VPS...\e[0m"
    apt-get update && apt-get install -y nginx || true
fi

# Tạo cấu hình Host Nginx trỏ về 127.0.0.1 (KHÔNG dùng Docker hostname 'app')
echo -e "\e[36m[Step] Đang tạo cấu hình Nginx Host Reverse Proxy...\e[0m"
cat > /etc/nginx/sites-available/${appName}.conf <<'NGINX_HOST_EOF'
server {
    listen 80;
    server_name ${domainName};
    client_max_body_size 20M;

    location / {
        proxy_pass http://127.0.0.1:${hostPort?c};
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
NGINX_HOST_EOF
ln -sf /etc/nginx/sites-available/${appName}.conf /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default 2>/dev/null || true
if nginx -t 2>/dev/null; then
    systemctl reload nginx || true
    echo -e "\e[32m[OK] Nginx Host Reverse Proxy đã được cấu hình thành công.\e[0m"
else
    echo -e "\e[33m[Warning] Nginx config test thất bại. Container sẽ vẫn truy cập được trực tiếp qua port ${hostPort?c}.\e[0m"
fi
</#if>

<#if installCertbot?? && installCertbot>
# --------------------------------------------------------------------
# 5. Cài đặt & Đăng ký Chứng chỉ SSL Let's Encrypt (Certbot)
# --------------------------------------------------------------------
if ! command -v certbot &> /dev/null; then
    echo -e "\e[36m[Step] Đang cài đặt Certbot & Plugin Nginx...\e[0m"
    apt-get update && apt-get install -y certbot python3-certbot-nginx || true
fi

<#if useSslipIo?? && useSslipIo>
# Tự động detect IP VPS và tạo domain sslip.io (không cần mua domain)
SSLIP_DOMAIN="$(curl -4s ifconfig.me || echo '127.0.0.1').sslip.io"
echo -e "\e[36m[Step] Sử dụng sslip.io: Tên miền tự động = $SSLIP_DOMAIN\e[0m"

# Cập nhật nginx config với domain sslip.io
if [ -f "/etc/nginx/sites-available/${appName}.conf" ]; then
    sed -i "s/server_name .*/server_name $SSLIP_DOMAIN;/" /etc/nginx/sites-available/${appName}.conf
    nginx -t || true
    systemctl reload nginx || true
fi

certbot --nginx -d "$SSLIP_DOMAIN" --non-interactive --agree-tos <#if adminEmail?? && adminEmail != "">-m "${adminEmail}"<#else>--register-unsafely-without-email</#if> || echo -e "\e[33m[Warning] Không thể cấp phát SSL cho $SSLIP_DOMAIN. Certbot rate-limit hoặc lỗi mạng.\e[0m"
<#else>
<#if domainName?? && domainName != "" && domainName != "localhost">
echo -e "\e[36m[Step] Đang đăng ký chứng chỉ HTTPS Let's Encrypt cho ${domainName}...\e[0m"
certbot --nginx -d ${domainName} --non-interactive --agree-tos <#if adminEmail?? && adminEmail != "">-m "${adminEmail}"<#else>--register-unsafely-without-email</#if> || echo -e "\e[33m[Warning] Không thể cấp phát SSL tự động cho ${domainName}. Hãy đảm bảo Domain đã trỏ A Record về IP VPS.\e[0m"
</#if>
</#if>
</#if>

echo -e "\e[32;1m[SETUP_OK] Hoàn tất quá trình Setup Hạ tầng Server!\e[0m"
