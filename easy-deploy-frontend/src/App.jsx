import { ConfigProvider, useConfig } from './context/ConfigContext';
import { VpsProvider } from './context/VpsContext';
import MainLayout from './layouts/MainLayout/MainLayout';
import GeneratorPage from './pages/GeneratorPage/GeneratorPage';
import WebTerminal from './features/terminal/WebTerminal';

function AppContent() {
  const { activeTab } = useConfig();

  return (
    <MainLayout>
      {activeTab === 'generator' && <GeneratorPage />}
      {activeTab === 'terminal' && <WebTerminal />}
    </MainLayout>
  );
}

function App() {
  return (
    <ConfigProvider>
      <VpsProvider>
        <AppContent />
      </VpsProvider>
    </ConfigProvider>
  );
}

export default App;
