import { useState } from 'react';
import { X, Mail, Lock, User, Eye, EyeOff, Loader2, AlertCircle, Layers, ArrowRight } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import './AuthModal.css';

function AuthModal() {
  const { authModalOpen, authModalTab, setAuthModalTab, closeAuthModal, login, register } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!authModalOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (authModalTab === 'login') {
        await login(email, password);
      } else {
        await register(fullName, email, password);
      }
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Đã có lỗi xảy ra. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  const handleTabSwitch = (tab) => {
    setError(null);
    setAuthModalTab(tab);
  };

  return (
    <div className="auth-overlay" onClick={closeAuthModal}>
      <div className="auth-modal" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="auth-header">
          <button type="button" className="auth-close-btn" onClick={closeAuthModal} title="Đóng">
            <X size={16} />
          </button>

          <div className="auth-logo-badge">
            <Layers size={24} />
          </div>

          <h3 className="auth-title">
            {authModalTab === 'login' ? 'Đăng Nhập' : 'Tạo Tài Khoản'}
          </h3>

          {/* Switch Tab */}
          <div className="auth-tabs">
            <button
              type="button"
              className={`auth-tab-btn ${authModalTab === 'login' ? 'auth-tab-btn--active' : ''}`}
              onClick={() => handleTabSwitch('login')}
            >
              Đăng Nhập
            </button>
            <button
              type="button"
              className={`auth-tab-btn ${authModalTab === 'register' ? 'auth-tab-btn--active' : ''}`}
              onClick={() => handleTabSwitch('register')}
            >
              Đăng Ký
            </button>
          </div>
        </div>

        {/* Form Body */}
        <div className="auth-body">
          <form className="auth-form" onSubmit={handleSubmit}>
            {error && (
              <div className="auth-error">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            {authModalTab === 'register' && (
              <div className="auth-field">
                <label className="auth-label">Họ và tên</label>
                <div className="auth-input-wrap">
                  <User size={16} className="auth-input-icon" />
                  <input
                    type="text"
                    required
                    className="auth-input"
                    placeholder="Nguyễn Văn A"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                  />
                </div>
              </div>
            )}

            <div className="auth-field">
              <label className="auth-label">Email</label>
              <div className="auth-input-wrap">
                <Mail size={16} className="auth-input-icon" />
                <input
                  type="email"
                  required
                  className="auth-input"
                  placeholder="name@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
            </div>

            <div className="auth-field">
              <label className="auth-label">Mật khẩu</label>
              <div className="auth-input-wrap">
                <Lock size={16} className="auth-input-icon" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  minLength={6}
                  className="auth-input"
                  placeholder="Ít nhất 6 ký tự"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
                <button
                  type="button"
                  className="auth-toggle-pwd"
                  onClick={() => setShowPassword(!showPassword)}
                  title={showPassword ? 'Ẩn mật khẩu' : 'Xem mật khẩu'}
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button type="submit" className="auth-submit-btn" disabled={loading}>
              {loading ? (
                <>
                  <Loader2 size={18} className="spin" />
                  <span>Đang xử lý...</span>
                </>
              ) : (
                <>
                  <span>{authModalTab === 'login' ? 'Đăng Nhập' : 'Tạo Tài Khoản'}</span>
                  <ArrowRight size={16} />
                </>
              )}
            </button>
          </form>

          <div className="auth-switch-prompt">
            {authModalTab === 'login' ? (
              <>
                Chưa có tài khoản?
                <button
                  type="button"
                  className="auth-switch-link"
                  onClick={() => handleTabSwitch('register')}
                >
                  Đăng ký ngay
                </button>
              </>
            ) : (
              <>
                Đã có tài khoản?
                <button
                  type="button"
                  className="auth-switch-link"
                  onClick={() => handleTabSwitch('login')}
                >
                  Đăng nhập
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default AuthModal;
