import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',
  timeout: 60000,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 502 || error.response?.status === 503 || error.code === 'ERR_NETWORK') {
      error.friendlyMessage = 'Không thể kết nối đến Backend máy chủ (Port 8088). Vui lòng khởi động backend Spring Boot.';
    } else if (error.response?.data?.message) {
      error.friendlyMessage = error.response.data.message;
    } else if (error.response?.data?.error) {
      error.friendlyMessage = error.response.data.error;
    } else {
      error.friendlyMessage = error.message || 'Đã có lỗi xảy ra.';
    }
    return Promise.reject(error);
  }
);

export default api;
