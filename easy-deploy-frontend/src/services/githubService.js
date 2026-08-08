import api from './api';

export const analyzeRepo = async (repoUrl) => {
  const { data } = await api.post('/github/analyze', { repoUrl });
  return data;
};
