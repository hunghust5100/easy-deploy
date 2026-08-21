import { useState, useRef, useEffect } from 'react';
import { PanelLeftClose, PanelLeftOpen, Layers, LogIn, UserPlus, LogOut, User, Shield, ChevronDown } from 'lucide-react';
import { useConfig } from '../../context/ConfigContext';
import { useAuth } from '../../context/AuthContext';
import './Header.css';

function Header() {
  const { sidebarCollapsed, toggleSidebar, setActiveTab } = useConfig();
  const { currentUser, isAuthenticated, openLoginModal, openRegisterModal, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown on click outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const getInitials = (name) => {
    if (!name) return 'U';
    const parts = name.trim().split(' ');
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  };

  return (
    <header className="header">
      <div className="header__brand">
        <button
          type="button"
          className="header__toggle-sidebar"
          onClick={toggleSidebar}
          title={sidebarCollapsed ? 'Hiện Sidebar' : 'Ẩn Sidebar'}
        >
          {sidebarCollapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
        </button>

        <div
          className="header__brand-click"
          onClick={() => setActiveTab('generator')}
          title="Về trang tạo cấu hình"
        >
          <div className="header__logo-icon">
            <Layers size={18} strokeWidth={2.4} />
          </div>
          <div className="header__brand-text">
            <h1 className="header__title">
              Easy<span className="header__title-accent">Deploy</span>
            </h1>
          </div>
        </div>
      </div>

      <nav className="header__nav">
        <a
          href="https://github.com/hunghust5100/easy-deploy"
          target="_blank"
          rel="noopener noreferrer"
          className="header__link"
          aria-label="GitHub Repository"
        >
          <svg viewBox="0 0 16 16" width="15" height="15" fill="currentColor" aria-hidden="true">
            <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z" />
          </svg>
          <span>GitHub</span>
        </a>

        {isAuthenticated && currentUser ? (
          <div className="header__user-menu" ref={dropdownRef}>
            <button
              type="button"
              className="header__user-trigger"
              onClick={() => setDropdownOpen(!dropdownOpen)}
            >
              <div className="header__avatar">
                {getInitials(currentUser.fullName || currentUser.email)}
              </div>
              <span className="header__user-name">{currentUser.fullName || currentUser.email}</span>
              <ChevronDown size={14} className="text-muted" />
            </button>

            {dropdownOpen && (
              <div className="header__dropdown">
                <div className="header__dropdown-info">
                  <div style={{ fontWeight: 600, fontSize: '0.85rem', color: '#0f172a' }}>
                    {currentUser.fullName}
                  </div>
                  <div className="header__dropdown-email">{currentUser.email}</div>
                  <span className="header__dropdown-role">{currentUser.role || 'DEVELOPER'}</span>
                </div>

                <button
                  type="button"
                  className="header__dropdown-item"
                  onClick={() => {
                    setDropdownOpen(false);
                    setActiveTab('projects');
                  }}
                >
                  <User size={15} />
                  <span>Dự Án Của Tôi</span>
                </button>

                <button
                  type="button"
                  className="header__dropdown-item"
                  onClick={() => {
                    setDropdownOpen(false);
                    setActiveTab('servers');
                  }}
                >
                  <Shield size={15} />
                  <span>Máy Chủ VPS</span>
                </button>

                <button
                  type="button"
                  className="header__dropdown-item header__dropdown-item--danger"
                  onClick={() => {
                    setDropdownOpen(false);
                    logout();
                  }}
                >
                  <LogOut size={15} />
                  <span>Đăng Xuất</span>
                </button>
              </div>
            )}
          </div>
        ) : (
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button
              type="button"
              className="header__auth-btn header__auth-btn--login"
              onClick={openLoginModal}
            >
              <LogIn size={14} />
              <span>Đăng Nhập</span>
            </button>
            <button
              type="button"
              className="header__auth-btn header__auth-btn--register"
              onClick={openRegisterModal}
            >
              <UserPlus size={14} />
              <span>Đăng Ký</span>
            </button>
          </div>
        )}
      </nav>
    </header>
  );
}

export default Header;
