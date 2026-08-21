import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { DEFAULT_CONFIG, FALLBACK_ENUMS } from '../constants/options';
import { previewConfig } from '../services/configService';
import { fetchEnums } from '../services/enumService';
import * as projectService from '../services/projectService';
import { useAuth } from './AuthContext';

const ConfigContext = createContext(null);
const LOCAL_CONFIG_KEY = 'easy_deploy_config';

export function sanitizeTechVersion(techStack, version) {
  if (!version) return '21';
  const str = String(version).trim();
  if (techStack === 'JAVA_MAVEN' || techStack === 'JAVA_GRADLE') {
    if (str === '8' || str === '1.8' || str === '8.0') return '8';
    if (str === '11' || str === '11.0') return '11';
    if (str === '17' || str === '17.0') return '17';
    if (str === '21' || str === '21.0') return '21';
    const num = parseInt(str.replace(/[^0-9]/g, ''), 10);
    if (!isNaN(num)) {
      if (num <= 8) return '8';
      if (num <= 11) return '11';
      if (num <= 17) return '17';
      return '21';
    }
    return '21';
  }
  return str;
}

export function ConfigProvider({ children }) {
  const { currentUser } = useAuth();
  const [enums, setEnums] = useState(FALLBACK_ENUMS);
  const [generatorStep, setGeneratorStep] = useState(1); // 1: Quét & Cấu hình, 2: Xem trước & Triển khai

  // Fetch enums từ API Backend
  useEffect(() => {
    fetchEnums().then((data) => {
      if (data) setEnums(data);
    });
  }, []);

  // Initialize activeTab from URL pathname
  const [activeTab, setActiveTabState] = useState(() => {
    const path = window.location.pathname;
    if (path.includes('projects')) return 'projects';
    if (path.includes('servers')) return 'servers';
    if (path.includes('history')) return 'history';
    if (path.includes('terminal')) return 'terminal';
    return 'generator';
  });

  // Custom setActiveTab that syncs browser URL
  const setActiveTab = useCallback((tab) => {
    setActiveTabState(tab);
    const targetPath = `/${tab}`;
    if (window.location.pathname !== targetPath) {
      window.history.pushState(null, '', targetPath);
    }
  }, []);

  useEffect(() => {
    if (window.location.pathname === '/' || window.location.pathname === '') {
      window.history.replaceState(null, '', '/generator');
    }

    const handlePopState = () => {
      const path = window.location.pathname;
      if (path.includes('projects')) setActiveTabState('projects');
      else if (path.includes('servers')) setActiveTabState('servers');
      else if (path.includes('history')) setActiveTabState('history');
      else if (path.includes('terminal')) setActiveTabState('terminal');
      else setActiveTabState('generator');
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const [config, setConfig] = useState(() => {
    try {
      const saved = localStorage.getItem(LOCAL_CONFIG_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        const stack = parsed.techStack || DEFAULT_CONFIG.techStack;
        const safeVer = sanitizeTechVersion(stack, parsed.techVersion || DEFAULT_CONFIG.techVersion);
        const safeServices = (parsed.services || []).map((s) => ({
          ...s,
          techVersion: sanitizeTechVersion(s.techStack, s.techVersion),
        }));
        return { ...DEFAULT_CONFIG, ...parsed, techVersion: safeVer, services: safeServices };
      }
      return { ...DEFAULT_CONFIG };
    } catch {
      return { ...DEFAULT_CONFIG };
    }
  });

  const [savedProjects, setSavedProjects] = useState([]);
  const [projectsLoading, setProjectsLoading] = useState(false);
  const [activeProjectId, setActiveProjectId] = useState(null);

  const [previewFiles, setPreviewFiles] = useState(null);
  const [originalPreviewFiles, setOriginalPreviewFiles] = useState(null);
  const [activeFileId, setActiveFileId] = useState('Dockerfile');
  const [previewLoading, setPreviewLoading] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  // Tải danh sách projects từ CSDL H2 theo User
  const refreshProjects = useCallback(async (userObj) => {
    const targetUser = userObj || currentUser;
    if (!targetUser?.id) {
      setSavedProjects([]);
      return;
    }
    setProjectsLoading(true);
    try {
      const list = await projectService.getProjects(targetUser.id);
      setSavedProjects(list);
    } catch (err) {
      console.warn('Không thể nạp danh sách dự án:', err.message);
    } finally {
      setProjectsLoading(false);
    }
  }, [currentUser]);

  useEffect(() => {
    refreshProjects(currentUser);
  }, [currentUser, refreshProjects]);

  // Lưu config vào LocalStorage
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
      if (field === 'dbType') {
        const match = enums.dbTypes?.find((d) => d.value === value);
        if (match) next.dbPort = match.defaultPort;
      }
      if (field === 'deployMode') {
        const isPull = typeof value === 'string' && value.toUpperCase() === 'REGISTRY_PULL';
        next.useDockerHub = isPull;
        next.deployMode = isPull ? 'REGISTRY_PULL' : 'REMOTE_BUILD';
      }
      return next;
    });
  }, [enums]);

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

  const toggleService = useCallback((serviceId) => {
    setConfig((prev) => {
      const currentServices = prev.services || [];
      const updatedServices = currentServices.map((s) =>
        s.id === serviceId ? { ...s, enabled: !s.enabled } : s
      );
      return { ...prev, services: updatedServices };
    });
  }, []);

  const updateService = useCallback((serviceId, updatedFields) => {
    setConfig((prev) => {
      const currentServices = prev.services || [];
      const updatedServices = currentServices.map((s) =>
        s.id === serviceId ? { ...s, ...updatedFields } : s
      );
      return { ...prev, services: updatedServices };
    });
  }, []);

  const addCustomService = useCallback((newService) => {
    setConfig((prev) => {
      const currentServices = prev.services || [];
      return { ...prev, services: [...currentServices, newService] };
    });
  }, []);

  const removeService = useCallback((serviceId) => {
    setConfig((prev) => {
      const currentServices = prev.services || [];
      return { ...prev, services: currentServices.filter((s) => s.id !== serviceId) };
    });
  }, []);

  const applyGithubResult = useCallback(async (suggestedConfig) => {
    if (!suggestedConfig) return;

    setConfig((prev) => {
      const techStack = suggestedConfig.techStack || prev.techStack;
      const rawVersion = suggestedConfig.techVersion || prev.techVersion;
      const safeTechVersion = sanitizeTechVersion(techStack, rawVersion);

      const rawServices = suggestedConfig.services && suggestedConfig.services.length > 0
        ? suggestedConfig.services
        : prev.services || [];

      const safeServices = rawServices.map((s) => ({
        ...s,
        techVersion: sanitizeTechVersion(s.techStack, s.techVersion),
      }));

      const updated = {
        ...prev,
        ...suggestedConfig,
        appName: suggestedConfig.appName || prev.appName,
        techStack,
        techVersion: safeTechVersion,
        appPort: suggestedConfig.appPort || prev.appPort,
        hostPort: suggestedConfig.hostPort || prev.hostPort,
        dbType: suggestedConfig.dbType || prev.dbType,
        dbPort: suggestedConfig.dbPort || prev.dbPort,
        deployPath: suggestedConfig.deployPath || `/root/${suggestedConfig.appName || prev.appName}`,
        services: safeServices,
      };

      try {
        localStorage.setItem(LOCAL_CONFIG_KEY, JSON.stringify(updated));
      } catch (e) {
        console.error(e);
      }

      fetchPreview(updated);
      return updated;
    });
  }, [fetchPreview]);

  // Tự động debounce fetch preview mỗi khi config thay đổi (bật/tắt CI/CD, Nginx, đổi branch...)
  useEffect(() => {
    const timer = setTimeout(() => {
      fetchPreview(config).then((files) => {
        if (files && activeFileId && !files[activeFileId]) {
          const firstKey = Object.keys(files)[0];
          if (firstKey) setActiveFileId(firstKey);
        }
      });
    }, 300);
    return () => clearTimeout(timer);
  }, [config, fetchPreview, activeFileId]);

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
    if (!previewFiles) {
      await fetchPreview();
    }
    setTimeout(() => {
      const el = document.getElementById('preview-section');
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 50);
  }, [previewFiles, fetchPreview]);

  // Lưu Project vào Database
  const saveProjectToDb = useCallback(async (customConfig = null) => {
    if (!currentUser?.id) return;
    const cfg = customConfig || config;

    const payload = {
      ...cfg,
      userId: currentUser.id,
      appName: cfg.appName || 'my-app',
    };

    try {
      if (activeProjectId) {
        const updated = await projectService.updateProject(activeProjectId, payload);
        setSavedProjects((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
        return updated;
      } else {
        const created = await projectService.createProject(payload);
        setSavedProjects((prev) => [created, ...prev]);
        setActiveProjectId(created.id);
        return created;
      }
    } catch (err) {
      console.error('Lỗi khi lưu project vào CSDL:', err);
      throw err;
    }
  }, [currentUser, config, activeProjectId]);

  // Nạp Project từ CSDL vào Generator
  const loadProjectFromDb = useCallback(async (projectId) => {
    try {
      const project = await projectService.getProjectById(projectId);
      if (project) {
        const stack = project.techStack || DEFAULT_CONFIG.techStack;
        const safeVer = sanitizeTechVersion(stack, project.techVersion || DEFAULT_CONFIG.techVersion);
        const safeServices = (project.services || []).map((s) => ({
          ...s,
          techVersion: sanitizeTechVersion(s.techStack, s.techVersion),
        }));

        const mappedConfig = {
          ...DEFAULT_CONFIG,
          appName: project.appName,
          repoUrl: project.repoUrl || '',
          gitBranch: project.gitBranch || 'main',
          techStack: stack,
          techVersion: safeVer,
          appPort: project.appPort,
          hostPort: project.hostPort,
          dbType: project.dbType,
          dbName: project.dbName,
          dbUser: project.dbUser,
          dbPort: project.dbPort,
          enableNginx: project.enableNginx,
          domainName: project.domainName || 'localhost',
          enableCicd: project.enableCicd,
          dockerHubUser: project.dockerHubUser || '',
          deployPath: project.deployPath || `/root/${project.appName}`,
          enableServerSetup: project.enableServerSetup,
          installNginx: project.installNginx,
          installCertbot: project.installCertbot,
          setupFirewall: project.setupFirewall,
          installDocker: project.installDocker,
          useSslipIo: project.useSslipIo,
          useDockerHub: project.useDockerHub,
          dockerHubUsername: project.dockerHubUsername || '',
          dockerImageTag: project.dockerImageTag || 'latest',
          deployMode: project.deployMode || 'remote_build',
          adminEmail: project.adminEmail || '',
          envVars: project.envVars || {},
          services: safeServices,
        };

        setConfig(mappedConfig);
        setActiveProjectId(project.id);
        fetchPreview(mappedConfig);
        setGeneratorStep(1);
        setActiveTab('generator');
        return project;
      }
    } catch (err) {
      console.error('Lỗi khi nạp project:', err);
      throw err;
    }
  }, [fetchPreview, setActiveTab]);

  // Xóa Project khỏi CSDL
  const deleteProjectFromDb = useCallback(async (projectId) => {
    try {
      await projectService.deleteProject(projectId);
      setSavedProjects((prev) => prev.filter((p) => p.id !== projectId));
      if (activeProjectId === projectId) {
        setActiveProjectId(null);
      }
    } catch (err) {
      console.error('Lỗi khi xóa project:', err);
      throw err;
    }
  }, [activeProjectId]);

  return (
    <ConfigContext.Provider
      value={{
        currentUser,
        activeTab,
        setActiveTab,
        generatorStep,
        setGeneratorStep,
        config,
        setConfig,
        updateField,
        applyGithubResult,
        toggleService,
        updateService,
        addCustomService,
        removeService,
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
        savedProjects,
        projectsLoading,
        activeProjectId,
        setActiveProjectId,
        refreshProjects,
        saveProjectToDb,
        loadProjectFromDb,
        deleteProjectFromDb,
        enums,
        sanitizeTechVersion,
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
