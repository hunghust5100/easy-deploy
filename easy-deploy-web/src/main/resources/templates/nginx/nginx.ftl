server {
    listen 80;
    server_name ${config.domainName};

    client_max_body_size 20M;

<#assign hasFrontend = false>
<#assign hasBackend = false>
<#assign frontendName = "">
<#assign frontendPort = 80>
<#assign backendName = "">
<#assign backendPort = 8080>

<#if config.services?? && (config.services?size > 1)>
  <#list config.services as service>
    <#if service.enabled>
      <#if service.frontend>
        <#assign hasFrontend = true>
        <#assign frontendName = service.name>
        <#assign frontendPort = service.containerPort>
      <#elseif service.backend>
        <#assign hasBackend = true>
        <#assign backendName = service.name>
        <#assign backendPort = service.containerPort>
      </#if>
    </#if>
  </#list>
</#if>

<#if hasFrontend && hasBackend>
    # Backend API Reverse Proxy
    location /api/ {
        proxy_pass http://${backendName}:${backendPort?c};
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket Reverse Proxy (Live Logs & Web SSH Terminal)
    location /ws/ {
        proxy_pass http://${backendName}:${backendPort?c};
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 86400s;
        proxy_send_timeout 86400s;
    }

    # Frontend Client Reverse Proxy
    location / {
        proxy_pass http://${frontendName}:${frontendPort?c};
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
<#else>
    location / {
        proxy_pass http://app:${config.appPort?c};
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
</#if>
}
