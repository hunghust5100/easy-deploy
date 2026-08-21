import api from './api';

const DEFAULT_USER_STORAGE_KEY = 'easy_deploy_current_user';

export const getUsers = async () => {
  const { data } = await api.get('/users');
  return data?.data || [];
};

export const getUserById = async (id) => {
  const { data } = await api.get(`/users/${id}`);
  return data?.data;
};

export const createUser = async (userData) => {
  const { data } = await api.post('/users', userData);
  return data?.data;
};

/**
 * Lấy hoặc tự động khởi tạo Developer User mặc định để gắn các tài nguyên CSDL
 */
export const getOrCreateDefaultUser = async () => {
  try {
    const savedUser = localStorage.getItem(DEFAULT_USER_STORAGE_KEY);
    if (savedUser) {
      const parsed = JSON.parse(savedUser);
      if (parsed?.id) return parsed;
    }

    const users = await getUsers();
    if (users && users.length > 0) {
      localStorage.setItem(DEFAULT_USER_STORAGE_KEY, JSON.stringify(users[0]));
      return users[0];
    }

    // Tạo default user
    const newUser = await createUser({
      email: 'hung.nk235100@sis.hust.edu.vn',
      fullName: 'Nguyễn Khánh Hưng',
      password: 'password123',
      role: 'DEVELOPER',
      status: 'ACTIVE',
    });

    localStorage.setItem(DEFAULT_USER_STORAGE_KEY, JSON.stringify(newUser));
    return newUser;
  } catch (error) {
    console.error('Lỗi khi lấy thông tin default user:', error);
    return {
      id: '00000000-0000-0000-0000-000000000001',
      fullName: 'Nguyễn Khánh Hưng',
      email: 'hung.nk235100@sis.hust.edu.vn',
    };
  }
};
