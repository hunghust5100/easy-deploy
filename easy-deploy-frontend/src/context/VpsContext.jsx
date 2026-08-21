import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import * as serverService from '../services/serverService';
import { useAuth } from './AuthContext';

const VpsContext = createContext(null);

export const DEFAULT_SNIPPETS = [
  { id: '1', title: 'Status Containers', command: 'docker compose ps', category: 'Docker' },
  { id: '2', title: 'View App Logs', command: 'docker compose logs --tail=100 -f', category: 'Docker' },
  { id: '3', title: 'Restart App Service', command: 'docker compose restart', category: 'Docker' },
  { id: '4', title: 'Check System Memory & Disk', command: 'free -h && df -h', category: 'System' },
  { id: '5', title: 'Check Nginx Status', command: 'systemctl status nginx', category: 'Nginx' },
  { id: '6', title: 'Check Firewall UFW', command: 'ufw status verbose', category: 'Security' },
  { id: '7', title: 'Clean Unused Docker Data', command: 'docker system prune -f', category: 'Docker' },
];

const LOCAL_SNIPPET_KEY = 'easy_deploy_snippets';

export function VpsProvider({ children }) {
  const { currentUser } = useAuth();
  const [vpsList, setVpsList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeVpsId, setActiveVpsId] = useState(null);

  const [snippets, setSnippets] = useState(() => {
    try {
      const saved = localStorage.getItem(LOCAL_SNIPPET_KEY);
      return saved ? JSON.parse(saved) : DEFAULT_SNIPPETS;
    } catch {
      return DEFAULT_SNIPPETS;
    }
  });

  // Tải danh sách VPS theo User đang đăng nhập
  const refreshServers = useCallback(async (userObj) => {
    const targetUser = userObj || currentUser;
    if (!targetUser?.id) {
      setVpsList([]);
      setActiveVpsId(null);
      return;
    }

    setLoading(true);
    try {
      const list = await serverService.getServers(targetUser.id);
      setVpsList(list);
      if (list.length > 0) {
        setActiveVpsId((prev) => (list.some((s) => s.id === prev) ? prev : list[0].id));
      } else {
        setActiveVpsId(null);
      }
    } catch (err) {
      console.warn('Không thể tải servers từ CSDL:', err.message);
    } finally {
      setLoading(false);
    }
  }, [currentUser]);

  useEffect(() => {
    refreshServers(currentUser);
  }, [currentUser, refreshServers]);

  useEffect(() => {
    localStorage.setItem(LOCAL_SNIPPET_KEY, JSON.stringify(snippets));
  }, [snippets]);

  const saveVpsProfile = useCallback(async (profile) => {
    if (!currentUser?.id) {
      throw new Error('Vui lòng đăng nhập để lưu máy chủ VPS');
    }
    try {
      const isUUID = (str) =>
        typeof str === 'string' &&
        /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(str);

      const serverPayload = {
        ...profile,
        userId: currentUser.id,
        sshPort: parseInt(profile.sshPort || profile.port) || 22,
        sshUser: (profile.sshUser || profile.username || 'root').trim(),
        defaultDeployPath: profile.defaultDeployPath || profile.deployPath || '/root',
      };

      if (profile.id && isUUID(profile.id.toString())) {
        // Cập nhật
        const updated = await serverService.updateServer(profile.id, serverPayload);
        setVpsList((prev) => prev.map((s) => (s.id === updated.id ? updated : s)));
        return updated;
      } else {
        // Thêm mới
        const created = await serverService.createServer(serverPayload);
        setVpsList((prev) => [created, ...prev]);
        setActiveVpsId(created.id);
        return created;
      }
    } catch (err) {
      console.error('Lỗi khi lưu server:', err);
      throw err;
    }
  }, [currentUser]);

  const deleteVpsProfile = useCallback(async (id) => {
    try {
      await serverService.deleteServer(id);
      setVpsList((prev) => prev.filter((p) => p.id !== id));
      if (activeVpsId === id) {
        setActiveVpsId(null);
      }
    } catch (err) {
      console.error('Lỗi khi xóa server:', err);
      throw err;
    }
  }, [activeVpsId]);

  const testConnection = useCallback(async (id) => {
    try {
      return await serverService.testServerConnection(id);
    } catch (err) {
      console.error('Lỗi test connection:', err);
      return { connected: false, message: err.message };
    }
  }, []);

  const addSnippet = useCallback((snippet) => {
    const newSnippet = {
      id: `snip-${Date.now()}`,
      title: snippet.title || 'Snippet mới',
      command: snippet.command || 'echo Hello',
      category: snippet.category || 'General',
    };
    setSnippets((prev) => [...prev, newSnippet]);
  }, []);

  const deleteSnippet = useCallback((id) => {
    setSnippets((prev) => prev.filter((s) => s.id !== id));
  }, []);

  return (
    <VpsContext.Provider
      value={{
        currentUser,
        vpsList,
        loading,
        activeVpsId,
        setActiveVpsId,
        refreshServers,
        saveVpsProfile,
        deleteVpsProfile,
        testConnection,
        snippets,
        addSnippet,
        deleteSnippet,
      }}
    >
      {children}
    </VpsContext.Provider>
  );
}

export function useVps() {
  const ctx = useContext(VpsContext);
  if (!ctx) throw new Error('useVps must be used within VpsProvider');
  return ctx;
}
