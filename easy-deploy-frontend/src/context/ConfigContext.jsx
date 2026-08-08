import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { DEFAULT_CONFIG, DB_TYPES } from '../constants/options';
import { previewConfig } from '../services/configService';

const ConfigContext = createContext(null);
const LOCAL_CONFIG_KEY = 'easy_deploy_config';

export function ConfigProvider({ children }) {
  // Initialize activeTab from URL pathname (/generator or /terminal)
  const [activeTab, setActiveTabState] = useState(() => {
    const path = window.location.pathname;
    return path.includes('terminal') ? 'terminal' : 'generator';
  });

  // Custom setActiveTab that syncs browser URL to /generator or /terminal
  const setActiveTab = useCallback((tab) => {
    setActiveTabState(tab);
    const targetPath = tab === 'terminal' ? '/terminal' : '/generator';
    if (window.location.pathname !== targetPath) {
      window.history.pushState(null, '', targetPath);
    }
  }, []);

  // Sync state on browser Back / Forward buttons (popstate) & initial redirect to /generator
  useEffect(() => {
    if (window.location.pathname === '/' || window.location.pathname === '') {
      window.history.replaceState(null, '', '/generator');
    }

    const handlePopState = () => {
      const path = window.location.pathname;
      setActiveTabState(path.includes('terminal') ? 'terminal' : 'generator');
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const [config, setConfig] = useState(() => {
    try {
      const saved = localStorage.getItem(LOCAL_CONFIG_KEY);
      return saved ? { ...DEFAULT_CONFIG, ...JSON.parse(saved) } : { ...DEFAULT_CONFIG };
    } catch {
      return { ...DEFAULT_CONFIG };
    }
  });
  const [previewFiles, setPreviewFiles] = useState(null);
  const [originalPreviewFiles, setOriginalPreviewFiles] = useState(null);
  const [activeFileId, setActiveFileId] = useState('Dockerfile');
  const [previewLoading, setPreviewLoading] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  // Lưu config vào LocalStorage mỗi khi thay đổi
  useEffect(() => {
    try {
      localStorage.setItem(LOCAL_CONFIG_KEY, JSON.stringify(config));
    } catch (e) {
      console.error('Lỗi lưu config vào LocalStorage:', e);
    }
  }, [config]);

  const toggleSidebar = useCallback(() => {
    setSidebarCollapsed((prev) => !prev);
  }, []);

  const updateField = useCallback((field, value) => {
    setConfig((prev) => {
      const next = { ...prev, [field]: value };

      /* Khi đổi dbType → tự động cập nhật dbPort mặc định */
      if (field === 'dbType') {
        const match = DB_TYPES.find((d) => d.value === value);
        if (match) next.dbPort = match.defaultPort;
      }

      return next;
    });
  }, []);

  const applyGithubResult = useCallback((suggestedConfig) => {
    setConfig((prev) => ({ ...prev, ...suggestedConfig }));
  }, []);

  const fetchPreview = useCallback(async (targetConfig = config) => {
    setPreviewLoading(true);
    try {
      const files = await previewConfig(targetConfig);
      setPreviewFiles(files);
      setOriginalPreviewFiles(files);
      return files;
    } catch (err) {
      console.error('Lỗi sinh preview:', err);
    } finally {
      setPreviewLoading(false);
    }
  }, [config]);

  // Tự động fetch preview ngay lần đầu nạp trang
  useEffect(() => {
    fetchPreview();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const updatePreviewFile = useCallback((filename, content) => {
    setPreviewFiles((prev) => ({
      ...prev,
      [filename]: content,
    }));
  }, []);

  const resetPreviewFile = useCallback((filename) => {
    if (originalPreviewFiles && originalPreviewFiles[filename] !== undefined) {
      setPreviewFiles((prev) => ({
        ...prev,
        [filename]: originalPreviewFiles[filename],
      }));
    }
  }, [originalPreviewFiles]);

  const selectAndPreviewFile = useCallback(async (fileId) => {
    setActiveFileId(fileId);

    // Tự động sinh preview nếu chưa có
    if (!previewFiles) {
      await fetchPreview();
    }

    // Tự động cuộn mượt xuống khu vực Kết quả
    setTimeout(() => {
      const el = document.getElementById('preview-section');
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 50);
  }, [previewFiles, fetchPreview]);

  return (
    <ConfigContext.Provider
      value={{
        activeTab,
        setActiveTab,
        config,
        setConfig,
        updateField,
        applyGithubResult,
        previewFiles,
        setPreviewFiles,
        originalPreviewFiles,
        updatePreviewFile,
        resetPreviewFile,
        activeFileId,
        setActiveFileId,
        previewLoading,
        fetchPreview,
        selectAndPreviewFile,
        sidebarCollapsed,
        setSidebarCollapsed,
        toggleSidebar,
      }}
    >
      {children}
    </ConfigContext.Provider>
  );
}

export function useConfig() {
  const ctx = useContext(ConfigContext);
  if (!ctx) throw new Error('useConfig must be used within ConfigProvider');
  return ctx;
}
