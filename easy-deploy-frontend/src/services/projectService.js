import api from './api';

export const getProjects = async (userId) => {
  const { data } = await api.get('/projects', {
    params: { userId },
  });
  return data?.data || [];
};

export const getProjectById = async (id) => {
  const { data } = await api.get(`/projects/${id}`);
  return data?.data;
};

export const createProject = async (projectData) => {
  const { data } = await api.post('/projects', projectData);
  return data?.data;
};

export const updateProject = async (id, projectData) => {
  const { data } = await api.put(`/projects/${id}`, projectData);
  return data?.data;
};

export const deleteProject = async (id) => {
  const { data } = await api.delete(`/projects/${id}`);
  return data?.data;
};
