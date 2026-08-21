import { AuthProvider } from './context/AuthContext';
import { ConfigProvider, useConfig } from './context/ConfigContext';
import { VpsProvider } from './context/VpsContext';
import MainLayout from './layouts/MainLayout/MainLayout';
import GeneratorPage from './pages/GeneratorPage/GeneratorPage';
import ProjectsPage from './pages/ProjectsPage/ProjectsPage';
import ServersPage from './pages/ServersPage/ServersPage';
import HistoryPage from './pages/HistoryPage/HistoryPage';
import WebTerminal from './features/terminal/WebTerminal';
import AuthModal from './features/auth/AuthModal';

function AppContent() {
  const { activeTab } = useConfig();

  return (
    <MainLayout>
      {activeTab === 'generator' && <GeneratorPage />}
      {activeTab === 'projects' && <ProjectsPage />}
      {activeTab === 'servers' && <ServersPage />}
      {activeTab === 'history' && <HistoryPage />}
      {activeTab === 'terminal' && <WebTerminal />}
      <AuthModal />
    </MainLayout>
  );
}

function App() {
  return (
    <AuthProvider>
      <ConfigProvider>
        <VpsProvider>
          <AppContent />
        </VpsProvider>
      </ConfigProvider>
    </AuthProvider>
  );
}

export default App;
