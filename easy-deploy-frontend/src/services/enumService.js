import api from './api';

let cachedEnums = null;

export const fetchEnums = async () => {
  if (cachedEnums) return cachedEnums;
  try {
    const { data } = await api.get('/enums');
    if (data?.data) {
      cachedEnums = data.data;
      return cachedEnums;
    }
  } catch (err) {
    console.warn('Không thể tải enums từ backend API, sử dụng fallback tĩnh:', err.message);
  }
  return null;
};
