import api from './api';

export const previewConfig = async (config) => {
  const { data } = await api.post('/preview', config);
  return data;
};
