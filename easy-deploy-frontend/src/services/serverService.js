import api from './api';

export const getServers = async (userId) => {
  const { data } = await api.get('/servers', {
    params: { userId },
  });
  return data?.data || [];
};

export const getServerById = async (id) => {
  const { data } = await api.get(`/servers/${id}`);
  return data?.data;
};

export const createServer = async (serverData) => {
  const { data } = await api.post('/servers', serverData);
  return data?.data;
};

export const updateServer = async (id, serverData) => {
  const { data } = await api.put(`/servers/${id}`, serverData);
  return data?.data;
};

export const deleteServer = async (id) => {
  const { data } = await api.delete(`/servers/${id}`);
  return data?.data;
};

export const testServerConnection = async (id) => {
  const { data } = await api.post(`/servers/${id}/test-connection`);
  return data?.data;
};
