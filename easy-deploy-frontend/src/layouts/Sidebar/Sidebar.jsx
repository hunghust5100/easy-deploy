import { Wrench, Terminal, Sparkles } from 'lucide-react';
import { useConfig } from '../../context/ConfigContext';
import './Sidebar.css';

function Sidebar() {
  const { activeTab, setActiveTab, sidebarCollapsed } = useConfig();

  return (
    <aside className={`sidebar ${sidebarCollapsed ? 'sidebar--collapsed' : ''}`}>
      {/* ── Section: Navigation Menu ── */}
      <div className="sidebar__section">
        <div className="sidebar__section-label">Menu Điều hướng</div>
        <nav className="sidebar__nav" aria-label="Main Navigation">
          <button
            type="button"
            className={`sidebar__nav-item ${activeTab === 'generator' ? 'sidebar__nav-item--active' : ''}`}
            onClick={() => setActiveTab('generator')}
          >
            <Wrench size={16} className="sidebar__nav-icon" />
            <span className="sidebar__nav-text">Config Generator</span>
          </button>

          <button
            type="button"
            className={`sidebar__nav-item ${activeTab === 'terminal' ? 'sidebar__nav-item--active' : ''}`}
            onClick={() => setActiveTab('terminal')}
          >
            <Terminal size={16} className="sidebar__nav-icon" />
            <span className="sidebar__nav-text">Web SSH Terminal</span>
          </button>
        </nav>
      </div>

      {/* ── Hint Footer ── */}
      <div className="sidebar__hint">
        <Sparkles size={14} className="sidebar__hint-icon" />
        <p>
          {activeTab === 'generator'
            ? 'Dán GitHub URL để tự động quét hoặc điền form cấu hình bên phải.'
            : 'Nhập thông tin VPS để mở kết nối Web SSH Terminal trực tiếp.'}
        </p>
      </div>
    </aside>
  );
}

export default Sidebar;
