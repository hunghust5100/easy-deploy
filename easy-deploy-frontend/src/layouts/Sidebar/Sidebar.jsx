import { Wrench, Terminal, FolderGit2, Server, History } from 'lucide-react';
import { useConfig } from '../../context/ConfigContext';
import './Sidebar.css';

function Sidebar() {
  const { activeTab, setActiveTab, sidebarCollapsed, savedProjects } = useConfig();

  const navItems = [
    { id: 'generator', label: 'Tạo Cấu Hình', icon: Wrench },
    { id: 'projects', label: 'Dự Án', icon: FolderGit2, badge: savedProjects?.length || null },
    { id: 'servers', label: 'Máy Chủ VPS', icon: Server },
    { id: 'history', label: 'Lịch Sử Deploy', icon: History },
    { id: 'terminal', label: 'Web Terminal', icon: Terminal },
  ];

  return (
    <aside className={`sidebar ${sidebarCollapsed ? 'sidebar--collapsed' : ''}`}>
      <div className="sidebar__section">
        <nav className="sidebar__nav" aria-label="Main Navigation">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                type="button"
                className={`sidebar__nav-item ${isActive ? 'sidebar__nav-item--active' : ''}`}
                onClick={() => setActiveTab(item.id)}
              >
                <div className="sidebar__nav-icon-wrap">
                  <Icon size={17} className="sidebar__nav-icon" />
                </div>
                <div className="sidebar__nav-info">
                  <span className="sidebar__nav-text">{item.label}</span>
                </div>
                {item.badge ? (
                  <span className="sidebar__nav-pill" style={{ background: '#4f46e5', color: '#fff' }}>
                    {item.badge}
                  </span>
                ) : null}
              </button>
            );
          })}
        </nav>
      </div>
    </aside>
  );
}

export default Sidebar;
