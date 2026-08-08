import { createContext, useContext, useState, useEffect, useCallback } from 'react';

const VpsContext = createContext(null);

export const DEFAULT_VPS_LIST = [];

export const DEFAULT_SNIPPETS = [
  { id: '1', title: 'Status Containers', command: 'docker compose ps', category: 'Docker' },
  { id: '2', title: 'View App Logs', command: 'docker compose logs --tail=100 -f', category: 'Docker' },
  { id: '3', title: 'Restart App Service', command: 'docker compose restart', category: 'Docker' },
  { id: '4', title: 'Check System Memory & Disk', command: 'free -h && df -h', category: 'System' },
  { id: '5', title: 'Check Nginx Status', command: 'systemctl status nginx', category: 'Nginx' },
  { id: '6', title: 'Check Firewall UFW', command: 'ufw status verbose', category: 'Security' },
  { id: '7', title: 'Clean Unused Docker Data', command: 'docker system prune -f', category: 'Docker' },
];

const LOCAL_VPS_KEY = 'easy_deploy_vps_list';
const LOCAL_SNIPPET_KEY = 'easy_deploy_snippets';

export function VpsProvider({ children }) {
  const [vpsList, setVpsList] = useState(() => {
    try {
      const saved = localStorage.getItem(LOCAL_VPS_KEY);
      if (!saved) return DEFAULT_VPS_LIST;
      const parsed = JSON.parse(saved);
      // Clean old mock data ('aws-prod', 'do-staging') if present in localStorage
      return parsed.filter((p) => p.id !== 'aws-prod' && p.id !== 'do-staging');
    } catch {
      return DEFAULT_VPS_LIST;
    }
  });

  const [snippets, setSnippets] = useState(() => {
    try {
      const saved = localStorage.getItem(LOCAL_SNIPPET_KEY);
      return saved ? JSON.parse(saved) : DEFAULT_SNIPPETS;
    } catch {
      return DEFAULT_SNIPPETS;
    }
  });

  const [activeVpsId, setActiveVpsId] = useState(vpsList[0]?.id || null);

  // Sync to LocalStorage
  useEffect(() => {
    localStorage.setItem(LOCAL_VPS_KEY, JSON.stringify(vpsList));
  }, [vpsList]);

  useEffect(() => {
    localStorage.setItem(LOCAL_SNIPPET_KEY, JSON.stringify(snippets));
  }, [snippets]);

  const saveVpsProfile = useCallback((profile) => {
    setVpsList((prev) => {
      const id = profile.id || `vps-${Date.now()}`;
      const updatedProfile = { ...profile, id };
      const existsIndex = prev.findIndex((p) => p.id === id);

      if (existsIndex >= 0) {
        const next = [...prev];
        next[existsIndex] = updatedProfile;
        return next;
      }
      return [...prev, updatedProfile];
    });
  }, []);

  const deleteVpsProfile = useCallback((id) => {
    setVpsList((prev) => prev.filter((p) => p.id !== id));
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
        vpsList,
        activeVpsId,
        setActiveVpsId,
        saveVpsProfile,
        deleteVpsProfile,
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
