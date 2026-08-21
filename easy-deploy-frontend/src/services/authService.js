import api from './api';

export const loginUser = async ({ email, password }) => {
  const { data } = await api.post('/auth/login', { email, password });
  return data?.data;
};

export const registerUser = async ({ fullName, email, password }) => {
  const { data } = await api.post('/auth/register', { fullName, email, password });
  return data?.data;
};

export const getMe = async (userId) => {
  const { data } = await api.get('/auth/me', {
    params: { userId },
  });
  return data?.data;
};
