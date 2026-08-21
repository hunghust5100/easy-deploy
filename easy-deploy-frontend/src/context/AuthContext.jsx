import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import * as authService from '../services/authService';

const AuthContext = createContext(null);
const AUTH_STORAGE_KEY = 'easy_deploy_auth_user';

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const saved = localStorage.getItem(AUTH_STORAGE_KEY);
      if (saved) return JSON.parse(saved);
    } catch {
      // fallback
    }
    return null; // Chưa đăng nhập → hiển thị nút Đăng Nhập / Đăng Ký
  });

  const [authModalOpen, setAuthModalOpen] = useState(false);
  const [authModalTab, setAuthModalTab] = useState('login'); // 'login' | 'register'

  useEffect(() => {
    if (currentUser) {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(currentUser));
    } else {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    }
  }, [currentUser]);

  const openLoginModal = useCallback(() => {
    setAuthModalTab('login');
    setAuthModalOpen(true);
  }, []);

  const openRegisterModal = useCallback(() => {
    setAuthModalTab('register');
    setAuthModalOpen(true);
  }, []);

  const closeAuthModal = useCallback(() => {
    setAuthModalOpen(false);
  }, []);

  const login = useCallback(async (email, password) => {
    try {
      const user = await authService.loginUser({ email, password });
      setCurrentUser(user);
      setAuthModalOpen(false);
      return user;
    } catch (err) {
      console.error('Đăng nhập thất bại:', err);
      throw err;
    }
  }, []);

  const register = useCallback(async (fullName, email, password) => {
    try {
      const user = await authService.registerUser({ fullName, email, password });
      setCurrentUser(user);
      setAuthModalOpen(false);
      return user;
    } catch (err) {
      console.error('Đăng ký thất bại:', err);
      throw err;
    }
  }, []);

  const logout = useCallback(() => {
    setCurrentUser(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        currentUser,
        isAuthenticated: !!currentUser?.id,
        authModalOpen,
        authModalTab,
        setAuthModalTab,
        openLoginModal,
        openRegisterModal,
        closeAuthModal,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
