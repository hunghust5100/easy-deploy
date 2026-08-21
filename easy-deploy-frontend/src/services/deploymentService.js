import api from './api';

export const getDeployments = async ({ projectId, userId } = {}) => {
  const params = {};
  if (projectId) params.projectId = projectId;
  if (userId) params.userId = userId;

  const { data } = await api.get('/deployments', { params });
  return data?.data || [];
};

export const getDeploymentById = async (id) => {
  const { data } = await api.get(`/deployments/${id}`);
  return data?.data;
};
