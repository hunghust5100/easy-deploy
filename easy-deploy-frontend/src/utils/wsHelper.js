/**
 * Helper sinh đường dẫn WebSocket linh hoạt, an toàn với môi trường HTTP/HTTPS và Custom Domain
 */
export function getWebSocketUrl(endpoint) {
  const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;

  // Nếu có cấu hình VITE_WS_URL trong .env
  if (import.meta.env.VITE_WS_URL) {
    const base = import.meta.env.VITE_WS_URL.replace(/\/+$/, '');
    return `${base}${cleanEndpoint}`;
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const hostname = window.location.hostname;

  // Nếu đang chạy local development trên Vite (port 5173 / localhost)
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return `${protocol}//${hostname}:8088${cleanEndpoint}`;
  }

  // Môi trường Production (sau reverse proxy hoặc domain chính)
  const port = window.location.port ? `:${window.location.port}` : '';
  return `${protocol}//${hostname}${port}${cleanEndpoint}`;
}
