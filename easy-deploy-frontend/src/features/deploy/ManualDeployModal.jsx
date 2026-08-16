import { useState } from 'react';
import { X, Copy, Check, ShieldCheck, Globe, Key, Server, FileText, ExternalLink, HelpCircle, ArrowRight } from 'lucide-react';
import { useConfig } from '../../context/ConfigContext';
import './ManualDeployModal.css';

function ManualDeployModal({ isOpen, onClose }) {
  const { config } = useConfig();
  const [activeTab, setActiveTab] = useState('github');
  const [copiedKey, setCopiedKey] = useState(null);

  if (!isOpen) return null;

  const appName = config.appName || 'my-app';

  const handleCopy = (text, key) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const tabs = [
    { id: 'github', label: '1. GitHub Secrets (CI/CD)', icon: Key },
    { id: 'vercel', label: '2. Vercel (Frontend)', icon: Globe },
    { id: 'vps', label: '3. VPS Firewall & DNS', icon: Server },
    { id: 'env', label: '4. Biến Bảo mật (.env)', icon: FileText },
  ];

  const githubSecrets = [
    { name: 'DOCKERHUB_USERNAME', value: 'my-dockerhub-user', desc: 'Tên tài khoản Docker Hub dùng để lưu trữ Container Image' },
    { name: 'DOCKERHUB_TOKEN', value: 'dckr_pat_xxxxx', desc: 'Personal Access Token từ Docker Hub Account Settings' },
    { name: 'SERVER_HOST', value: '103.179.x.x', desc: 'Địa chỉ IP Public hoặc Domain đại diện cho VPS của bạn' },
    { name: 'SERVER_USER', value: 'root', desc: 'Tên người dùng SSH trên VPS (Mặc định: root)' },
    { name: 'SERVER_PASSWORD', value: '••••••••', desc: 'Mật khẩu SSH (hoặc dùng SERVER_SSH_KEY cho RSA Key)' },
  ];

  const envExample = `# File Biến Môi trường Production (.env trên VPS)
# Đường dẫn lưu trữ: /root/${appName}/.env
APP_NAME=${appName}
APP_PORT=${config.hostPort || 8080}
DATABASE_URL=jdbc:postgresql://db:5432/${appName}_db
DATABASE_USER=postgres
DATABASE_PASSWORD=ChangeMeInProduction_SuperSecret123!
JWT_SECRET=super_secret_jwt_key_change_in_production_32chars
`;

  return (
    <div className="md-overlay" onClick={onClose}>
      <div className="md-modal" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div className="md-header">
          <div className="md-title">
            <div className="md-title-icon-wrap">
              <ShieldCheck size={20} className="md-title-icon" />
            </div>
            <div>
              <h3 className="md-title-text">Hướng dẫn Cấu hình Bắt buộc Ngoài Hệ Thống</h3>
              <span className="md-subtitle">Các thiết lập thủ công người dùng cần cấu hình trên GitHub, Vercel & VPS</span>
            </div>
          </div>
          <button type="button" className="md-close" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        {/* Tab Navigation */}
        <div className="md-tabs">
          {tabs.map((t) => {
            const Icon = t.icon;
            return (
              <button
                key={t.id}
                type="button"
                className={`md-tab ${activeTab === t.id ? 'active' : ''}`}
                onClick={() => setActiveTab(t.id)}
              >
                <Icon size={14} />
                <span>{t.label}</span>
              </button>
            );
          })}
        </div>

        {/* Modal Body */}
        <div className="md-body">
          <div className="md-banner">
            <HelpCircle size={16} className="md-banner-icon" />
            <span>
              EasyDeploy đã tự động hóa 100% việc sinh Dockerfile, Nginx & Pipeline. Dưới đây là các thông tin bí mật và hạ tầng bên ngoài bạn cần khai báo:
            </span>
          </div>

          {/* TAB 1: GITHUB SECRETS */}
          {activeTab === 'github' && (
            <div className="md-tab-content">
              <div className="md-section-header">
                <div>
                  <h4 className="md-section-title">Khóa Bảo mật Repository (GitHub Actions Secrets)</h4>
                  <p className="md-desc">
                    Vào GitHub Repository &gt; <code>Settings</code> &gt; <code>Secrets and variables</code> &gt; <code>Actions</code> &gt; <code>New repository secret</code>:
                  </p>
                </div>
              </div>

              <div className="md-secrets-grid">
                {githubSecrets.map((s) => (
                  <div key={s.name} className="md-secret-card">
                    <div className="md-secret-card__top">
                      <code className="md-secret-name">{s.name}</code>
                      <button
                        type="button"
                        className="md-copy-btn"
                        onClick={() => handleCopy(s.name, s.name)}
                        title="Sao chép tên biến"
                      >
                        {copiedKey === s.name ? <Check size={14} className="copied text-success" /> : <Copy size={14} />}
                      </button>
                    </div>
                    <span className="md-secret-desc">{s.desc}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* TAB 2: VERCEL FRONTEND */}
          {activeTab === 'vercel' && (
            <div className="md-tab-content">
              <div className="md-section-header">
                <div>
                  <h4 className="md-section-title">Triển khai Giao diện Frontend lên Vercel</h4>
                  <p className="md-desc">
                    Dành cho trường hợp bạn tách riêng Frontend (React/Vite/Next.js) và muốn deploy lên CDN toàn cầu của Vercel:
                  </p>
                </div>
                <a
                  href="https://vercel.com/new"
                  target="_blank"
                  rel="noreferrer"
                  className="md-link-btn"
                >
                  <span>Mở Vercel Dashboard</span>
                  <ExternalLink size={13} />
                </a>
              </div>

              <div className="md-timeline">
                <div className="md-timeline-item">
                  <div className="md-timeline-badge">1</div>
                  <div className="md-timeline-content">
                    <h5>Import Repository</h5>
                    <p>Đăng nhập Vercel, chọn <strong>Add New &gt; Project</strong> và import Repo GitHub của bạn.</p>
                  </div>
                </div>

                <div className="md-timeline-item">
                  <div className="md-timeline-badge">2</div>
                  <div className="md-timeline-content">
                    <h5>Cấu hình Thông số Build</h5>
                    <div className="md-params-box">
                      <span className="md-param-pill"><strong>Framework:</strong> Vite / React</span>
                      <span className="md-param-pill"><strong>Root Directory:</strong> <code>frontend</code></span>
                      <span className="md-param-pill"><strong>Build Command:</strong> <code>npm run build</code></span>
                      <span className="md-param-pill"><strong>Output Directory:</strong> <code>dist</code></span>
                    </div>
                  </div>
                </div>

                <div className="md-timeline-item">
                  <div className="md-timeline-badge">3</div>
                  <div className="md-timeline-content">
                    <h5>Cấu hình Biến môi trường API</h5>
                    <p>Tại mục <strong>Environment Variables</strong>, thêm biến trỏ về Backend API VPS của bạn:</p>
                    <div className="md-code-box">
                      <code>VITE_API_BASE_URL=https://api.yourdomain.com</code>
                      <button
                        type="button"
                        className="md-copy-btn"
                        onClick={() => handleCopy('VITE_API_BASE_URL=https://api.yourdomain.com', 'vercel_env')}
                      >
                        {copiedKey === 'vercel_env' ? <Check size={14} className="copied text-success" /> : <Copy size={14} />}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 3: VPS FIREWALL & DNS */}
          {activeTab === 'vps' && (
            <div className="md-tab-content">
              <h4 className="md-section-title">Thiết lập Firewall & DNS Máy chủ VPS</h4>
              <p className="md-desc">
                Đảm bảo VPS mở cổng truyền thông và tên miền trỏ đúng địa chỉ IP máy chủ:
              </p>

              <div className="md-vps-cards">
                {/* Firewall Card */}
                <div className="md-vps-box">
                  <div className="md-vps-box__header">
                    <Server size={15} />
                    <span>1. Mở cổng Firewall (Inbound Rules)</span>
                  </div>
                  <p className="md-desc">Mở cổng HTTP (80), HTTPS (443) và SSH (22) trên Cloud Firewall hoặc ufw:</p>
                  <div className="md-code-box">
                    <code>sudo ufw allow 80/tcp && sudo ufw allow 443/tcp && sudo ufw allow 22/tcp</code>
                    <button
                      type="button"
                      className="md-copy-btn"
                      onClick={() => handleCopy('sudo ufw allow 80/tcp && sudo ufw allow 443/tcp && sudo ufw allow 22/tcp', 'ufw')}
                    >
                      {copiedKey === 'ufw' ? <Check size={14} className="copied text-success" /> : <Copy size={14} />}
                    </button>
                  </div>
                </div>

                {/* DNS Table Card */}
                <div className="md-vps-box">
                  <div className="md-vps-box__header">
                    <Globe size={15} />
                    <span>2. Cấu hình Bản ghi Tên miền (DNS A Record)</span>
                  </div>
                  <p className="md-desc">Tại quản trị Domain (Cloudflare/Vietnix/GoDaddy), thêm bản ghi A Record:</p>
                  <table className="md-table">
                    <thead>
                      <tr>
                        <th>Loại (Type)</th>
                        <th>Tên (Host/Name)</th>
                        <th>Giá trị (Value/Target)</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td><span className="badge badge--info">A</span></td>
                        <td><code>@</code> (hoặc <code>api</code>)</td>
                        <td><code>IP_PUBLIC_VPS_CỦA_BẠN</code></td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                {/* Docker Install */}
                <div className="md-vps-box">
                  <div className="md-vps-box__header">
                    <FileText size={15} />
                    <span>3. Script Cài đặt Docker Nhanh (Dành cho VPS mới)</span>
                  </div>
                  <div className="md-code-box">
                    <code>curl -fsSL https://get.docker.com | sh</code>
                    <button
                      type="button"
                      className="md-copy-btn"
                      onClick={() => handleCopy('curl -fsSL https://get.docker.com | sh', 'docker_install')}
                    >
                      {copiedKey === 'docker_install' ? <Check size={14} className="copied text-success" /> : <Copy size={14} />}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 4: BIẾN BẢO MẬT .ENV */}
          {activeTab === 'env' && (
            <div className="md-tab-content">
              <h4 className="md-section-title">Khởi tạo File Môi trường Bảo mật (.env)</h4>
              <p className="md-desc">
                Để bảo mật, không commit mật khẩu Database hay JWT key lên Git. Hãy tạo file <code>.env</code> trực tiếp tại thư mục <code>/root/{appName}/.env</code> trên VPS:
              </p>

              <div className="md-code-editor">
                <div className="md-code-editor__header">
                  <span>/root/{appName}/.env</span>
                  <button
                    type="button"
                    className="md-copy-btn"
                    onClick={() => handleCopy(envExample, 'env_file')}
                  >
                    {copiedKey === 'env_file' ? <Check size={14} className="copied text-success" /> : <Copy size={14} />}
                    <span style={{ fontSize: '11px', marginLeft: '4px' }}>Sao chép mẫu .env</span>
                  </button>
                </div>
                <pre className="md-code-editor__content">{envExample}</pre>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ManualDeployModal;
