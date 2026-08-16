import { Wrench, Terminal, Sparkles, Cpu, HelpCircle } from 'lucide-react';
import { useConfig } from '../../context/ConfigContext';
import './Sidebar.css';

function Sidebar() {
  const { activeTab, setActiveTab, sidebarCollapsed } = useConfig();

  return (
    <aside className={`sidebar ${sidebarCollapsed ? 'sidebar--collapsed' : ''}`}>
      {/* ── Section: Navigation Menu ── */}
      <div className="sidebar__section">
        <div className="sidebar__section-label">Menu Điều Hướng</div>
        <nav className="sidebar__nav" aria-label="Main Navigation">
          <button
            type="button"
            className={`sidebar__nav-item ${activeTab === 'generator' ? 'sidebar__nav-item--active' : ''}`}
            onClick={() => setActiveTab('generator')}
          >
            <div className="sidebar__nav-icon-wrap">
              <Wrench size={16} className="sidebar__nav-icon" />
            </div>
            <div className="sidebar__nav-info">
              <span className="sidebar__nav-text">Config Generator</span>
              <span className="sidebar__nav-sub">Sinh Docker & DevOps</span>
            </div>
            <span className="sidebar__nav-pill">Auto</span>
          </button>

          <button
            type="button"
            className={`sidebar__nav-item ${activeTab === 'terminal' ? 'sidebar__nav-item--active' : ''}`}
            onClick={() => setActiveTab('terminal')}
          >
            <div className="sidebar__nav-icon-wrap">
              <Terminal size={16} className="sidebar__nav-icon" />
            </div>
            <div className="sidebar__nav-info">
              <span className="sidebar__nav-text">Web SSH Terminal</span>
              <span className="sidebar__nav-sub">Quản trị VPS từ xa</span>
            </div>
            <span className="sidebar__nav-pill">Live</span>
          </button>
        </nav>
      </div>

      {/* ── Capabilities Highlights ── */}
      <div className="sidebar__feature-box">
        <div className="sidebar__feature-header">
          <Cpu size={14} className="sidebar__feature-icon" />
          <span>Hỗ Trợ Toàn Diện</span>
        </div>
        <div className="sidebar__feature-tags">
          <span className="sidebar__tag">Java 17-25</span>
          <span className="sidebar__tag">Node/React</span>
          <span className="sidebar__tag">Python</span>
          <span className="sidebar__tag">Go / Rust</span>
          <span className="sidebar__tag">Nginx SSL</span>
          <span className="sidebar__tag">Docker V2</span>
        </div>
      </div>

      {/* ── Hint Footer ── */}
      <div className="sidebar__hint">
        <div className="sidebar__hint-icon-wrap">
          <Sparkles size={14} className="sidebar__hint-icon" />
        </div>
        <p>
          {activeTab === 'generator'
            ? 'Dán GitHub URL để quét công nghệ tự động hoặc tùy chỉnh cấu hình bên phải.'
            : 'Nhập thông tin VPS để mở phiên Web SSH Terminal tương tác trực tiếp.'}
        </p>
      </div>
    </aside>
  );
}

export default Sidebar;
