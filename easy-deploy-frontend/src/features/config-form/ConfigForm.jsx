import { useState } from 'react';
import { useConfig } from '../../context/ConfigContext';
import { TECH_STACKS, DB_TYPES, DEPLOY_MODES } from '../../constants/options';
import {
  AppWindow,
  Database,
  Globe,
  GitBranch,
  Server,
  ShieldCheck,
  ChevronDown,
  Settings,
  Layers,
  Sparkles,
  Container,
  CheckCircle,
  Hash,
  Lock,
  User,
  Folder,
} from 'lucide-react';
import './ConfigForm.css';

function ConfigForm() {
  const { config, updateField } = useConfig();
  const [showAdvancedSSL, setShowAdvancedSSL] = useState(false);

  return (
    <div className="config-form">
      {/* ── 1. App Info Card ── */}
      <div className="cf-card">
        <div className="cf-card__header">
          <div className="cf-card__icon-wrap">
            <AppWindow size={16} />
          </div>
          <div className="cf-card__title-group">
            <h3 className="cf-card__title">1. Thông tin Ứng dụng & Nền tảng</h3>
            <p className="cf-card__desc">Cấu hình tên project, tech stack nền tảng và cổng mạng (port mapping).</p>
          </div>
        </div>

        <div className="cf-grid">
          <div className="cf-field">
            <label htmlFor="appName">Tên Ứng dụng (App Name)</label>
            <div className="cf-input-wrapper">
              <input
                id="appName"
                type="text"
                value={config.appName}
                onChange={(e) => updateField('appName', e.target.value.toLowerCase().replaceAll(/[^a-z0-9_-]/g, '-'))}
                placeholder="my-app"
              />
            </div>
          </div>

          <div className="cf-field">
            <label htmlFor="techStack">Nền tảng Công nghệ (Tech Stack)</label>
            <div className="cf-input-wrapper">
              <select
                id="techStack"
                value={config.techStack}
                onChange={(e) => updateField('techStack', e.target.value)}
              >
                {TECH_STACKS.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="cf-field">
            <label htmlFor="techVersion">Phiên bản Runtime</label>
            <div className="cf-input-wrapper">
              <input
                id="techVersion"
                type="text"
                value={config.techVersion}
                onChange={(e) => updateField('techVersion', e.target.value)}
                placeholder="e.g. 21, 20, 3.11"
              />
            </div>
          </div>

          <div className="cf-field">
            <label htmlFor="appPort">Cổng Container (App Port)</label>
            <div className="cf-input-wrapper">
              <input
                id="appPort"
                type="number"
                value={config.appPort}
                onChange={(e) => updateField('appPort', parseInt(e.target.value) || 0)}
              />
            </div>
          </div>

          <div className="cf-field">
            <label htmlFor="hostPort">Cổng VPS Mở ngoài (Host Port)</label>
            <div className="cf-input-wrapper">
              <input
                id="hostPort"
                type="number"
                value={config.hostPort}
                onChange={(e) => updateField('hostPort', parseInt(e.target.value) || 0)}
              />
            </div>
          </div>
        </div>
      </div>

      {/* ── 2. Deployment Strategy Card ── */}
      <div className="cf-card">
        <div className="cf-card__header">
          <div className="cf-card__icon-wrap">
            <Container size={16} />
          </div>
          <div className="cf-card__title-group">
            <h3 className="cf-card__title">2. Chiến lược Triển khai & Docker Hub Registry</h3>
            <p className="cf-card__desc">Chọn phương thức build image trực tiếp trên VPS hoặc kéo từ Docker Hub.</p>
          </div>
        </div>

        <div className="cf-strategy-selector">
          {DEPLOY_MODES.map((mode) => {
            const isSelected = (config.deployMode || 'remote_build') === mode.value;
            return (
              <div
                key={mode.value}
                className={`cf-strategy-card ${isSelected ? 'cf-strategy-card--active' : ''}`}
                onClick={() => {
                  updateField('deployMode', mode.value);
                  if (mode.value === 'registry_pull') {
                    updateField('useDockerHub', true);
                  }
                }}
              >
                <div className="cf-strategy-card__header">
                  <span className="cf-strategy-card__title">{mode.label}</span>
                  {isSelected && (
                    <span className="badge badge--accent">
                      <CheckCircle size={11} /> Đang chọn
                    </span>
                  )}
                </div>
                <p className="cf-strategy-card__desc">{mode.desc}</p>
              </div>
            );
          })}
        </div>

        {((config.deployMode === 'registry_pull') || config.useDockerHub) && (
          <div className="cf-grid cf-registry-box">
            <div className="cf-field">
              <label htmlFor="dockerHubUsername">Docker Hub Username *</label>
              <div className="cf-input-wrapper">
                <input
                  id="dockerHubUsername"
                  type="text"
                  value={config.dockerHubUsername || ''}
                  placeholder="e.g. mydockerhubuser"
                  onChange={(e) => {
                    updateField('dockerHubUsername', e.target.value);
                    updateField('dockerHubUser', e.target.value);
                  }}
                />
              </div>
            </div>

            <div className="cf-field">
              <label htmlFor="dockerImageTag">Docker Image Tag</label>
              <div className="cf-input-wrapper">
                <input
                  id="dockerImageTag"
                  type="text"
                  value={config.dockerImageTag || 'latest'}
                  placeholder="e.g. latest, v1.0.0"
                  onChange={(e) => updateField('dockerImageTag', e.target.value)}
                />
              </div>
            </div>

            <div className="cf-field">
              <label htmlFor="dockerHubToken">Access Token / Password (Tùy chọn)</label>
              <div className="cf-input-wrapper">
                <input
                  id="dockerHubToken"
                  type="password"
                  value={config.dockerHubToken || ''}
                  placeholder="dckr_pat_xxxxx"
                  onChange={(e) => updateField('dockerHubToken', e.target.value)}
                />
              </div>
            </div>

            <div className="cf-field cf-field--full">
              <div className="cf-docker-preview">
                <span className="cf-docker-preview__label">📦 Image Target:</span>
                <code className="cf-docker-preview__code">
                  docker.io/{config.dockerHubUsername || 'username'}/{config.appName}:{config.dockerImageTag || 'latest'}
                </code>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* ── 3. Database Card ── */}
      <div className="cf-card">
        <div className="cf-card__header">
          <div className="cf-card__icon-wrap">
            <Database size={16} />
          </div>
          <div className="cf-card__title-group">
            <h3 className="cf-card__title">3. Cơ sở Dữ liệu & Caching</h3>
            <p className="cf-card__desc">Tự động cấu hình Docker container cho database kèm persistent volume lưu trữ.</p>
          </div>
        </div>

        <div className="cf-grid">
          <div className="cf-field">
            <label htmlFor="dbType">Loại CSDL</label>
            <div className="cf-input-wrapper">
              <select
                id="dbType"
                value={config.dbType}
                onChange={(e) => updateField('dbType', e.target.value)}
              >
                {DB_TYPES.map((d) => (
                  <option key={d.value} value={d.value}>{d.label}</option>
                ))}
              </select>
            </div>
          </div>

          {config.dbType !== 'NONE' && (
            <>
              <div className="cf-field">
                <label htmlFor="dbName">Tên Database</label>
                <div className="cf-input-wrapper">
                  <input
                    id="dbName"
                    type="text"
                    value={config.dbName}
                    onChange={(e) => updateField('dbName', e.target.value)}
                  />
                </div>
              </div>
              <div className="cf-field">
                <label htmlFor="dbUser">Username</label>
                <div className="cf-input-wrapper">
                  <input
                    id="dbUser"
                    type="text"
                    value={config.dbUser}
                    onChange={(e) => updateField('dbUser', e.target.value)}
                  />
                </div>
              </div>
              <div className="cf-field">
                <label htmlFor="dbPass">Password</label>
                <div className="cf-input-wrapper">
                  <input
                    id="dbPass"
                    type="password"
                    value={config.dbPass}
                    onChange={(e) => updateField('dbPass', e.target.value)}
                  />
                </div>
              </div>
              <div className="cf-field">
                <label htmlFor="dbPort">Cổng Database</label>
                <div className="cf-input-wrapper">
                  <input
                    id="dbPort"
                    type="number"
                    value={config.dbPort}
                    onChange={(e) => updateField('dbPort', parseInt(e.target.value) || 0)}
                  />
                </div>
              </div>
            </>
          )}
        </div>
      </div>

      {/* ── 4. Nginx Reverse Proxy Card ── */}
      <div className="cf-card">
        <div className="cf-card__header">
          <div className="cf-card__icon-wrap">
            <Globe size={16} />
          </div>
          <div className="cf-card__title-group">
            <h3 className="cf-card__title">4. Nginx Reverse Proxy</h3>
            <p className="cf-card__desc">Sinh file cấu hình Nginx tối ưu chuyển tiếp cổng, nén gzip và bảo mật headers.</p>
          </div>
        </div>

        <div className="cf-grid">
          <div className="cf-field cf-field--toggle">
            <div>
              <span className="cf-toggle-label">Bật Nginx Reverse Proxy</span>
              <span className="cf-toggle-desc">Tạo file <code>nginx.conf</code> tiêu chuẩn production</span>
            </div>
            <button
              id="enableNginx"
              type="button"
              role="switch"
              aria-checked={config.enableNginx}
              className={`cf-toggle ${config.enableNginx ? 'cf-toggle--on' : ''}`}
              onClick={() => updateField('enableNginx', !config.enableNginx)}
            >
              <span className="cf-toggle__thumb" />
            </button>
          </div>

          {config.enableNginx && (
            <div className="cf-field cf-field--full">
              <label htmlFor="domainName">Tên miền Nginx Reverse Proxy (Domain Name)</label>
              <div className="cf-input-wrapper">
                <input
                  id="domainName"
                  type="text"
                  value={config.domainName}
                  placeholder="e.g. localhost hoặc api.yourdomain.com"
                  onChange={(e) => updateField('domainName', e.target.value)}
                />
              </div>
              <p className="cf-field-hint">
                🌐 Tên miền để Nginx lắng nghe và chuyển tiếp traffic (directive <code>server_name</code>).
              </p>
            </div>
          )}
        </div>
      </div>

      {/* ── 5. CI/CD GitHub Actions Card ── */}
      <div className="cf-card">
        <div className="cf-card__header">
          <div className="cf-card__icon-wrap">
            <GitBranch size={16} />
          </div>
          <div className="cf-card__title-group">
            <h3 className="cf-card__title">5. CI/CD Pipeline (GitHub Actions)</h3>
            <p className="cf-card__desc">Sinh workflow <code>.github/workflows/deploy-github.yml</code> tự động build & SSH deploy khi push code.</p>
          </div>
        </div>

        <div className="cf-grid">
          <div className="cf-field cf-field--toggle">
            <div>
              <span className="cf-toggle-label">Bật GitHub Actions CI/CD</span>
              <span className="cf-toggle-desc">Tự động kích hoạt khi có commit mới lên Git branch</span>
            </div>
            <button
              id="enableCicd"
              type="button"
              role="switch"
              aria-checked={config.enableCicd}
              className={`cf-toggle ${config.enableCicd ? 'cf-toggle--on' : ''}`}
              onClick={() => updateField('enableCicd', !config.enableCicd)}
            >
              <span className="cf-toggle__thumb" />
            </button>
          </div>

          {config.enableCicd && (
            <>
              <div className="cf-field">
                <label htmlFor="gitBranch">Git Branch</label>
                <div className="cf-input-wrapper">
                  <input
                    id="gitBranch"
                    type="text"
                    value={config.gitBranch || 'main'}
                    placeholder="e.g. main, master"
                    onChange={(e) => updateField('gitBranch', e.target.value)}
                  />
                </div>
              </div>

              <div className="cf-field">
                <label htmlFor="dockerHubUser">Docker Hub Username (cho CI/CD)</label>
                <div className="cf-input-wrapper">
                  <input
                    id="dockerHubUser"
                    type="text"
                    value={config.dockerHubUser || config.dockerHubUsername || ''}
                    placeholder="Username Docker Hub"
                    onChange={(e) => {
                      updateField('dockerHubUser', e.target.value);
                      if (!config.dockerHubUsername) updateField('dockerHubUsername', e.target.value);
                    }}
                  />
                </div>
              </div>

              <div className="cf-field">
                <label htmlFor="deployPath">Thư mục Deploy trên VPS</label>
                <div className="cf-input-wrapper">
                  <input
                    id="deployPath"
                    type="text"
                    value={config.deployPath || ''}
                    placeholder="e.g. /root/my-app"
                    onChange={(e) => updateField('deployPath', e.target.value)}
                  />
                </div>
              </div>
            </>
          )}
        </div>
      </div>

      {/* ── 6. Bootstrap Server Script & SSL Card ── */}
      <div className="cf-card">
        <div className="cf-card__header">
          <div className="cf-card__icon-wrap">
            <ShieldCheck size={16} />
          </div>
          <div className="cf-card__title-group">
            <h3 className="cf-card__title">6. Bootstrap Server & Bảo mật SSL Miễn Phí</h3>
            <p className="cf-card__desc">Sinh script <code>setup-server.sh</code> tự động cài đặt môi trường từ A-Z cho máy chủ VPS mới tinh.</p>
          </div>
        </div>

        <div className="cf-grid">
          <div className="cf-field cf-field--toggle cf-field--full">
            <div>
              <span className="cf-toggle-label">Sinh script Bootstrap Server (setup-server.sh)</span>
              <span className="cf-toggle-desc">Tự động cài đặt Docker, Nginx, SSL và bật tường lửa UFW</span>
            </div>
            <button
              id="enableServerSetup"
              type="button"
              role="switch"
              aria-checked={config.enableServerSetup}
              className={`cf-toggle ${config.enableServerSetup ? 'cf-toggle--on' : ''}`}
              onClick={() => updateField('enableServerSetup', !config.enableServerSetup)}
            >
              <span className="cf-toggle__thumb" />
            </button>
          </div>

          {config.enableServerSetup && (
            <>
              <div className="cf-checkboxes cf-field--full">
                <label className="cf-checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.installDocker}
                    onChange={(e) => updateField('installDocker', e.target.checked)}
                  />
                  <div className="cf-checkbox-text">
                    <span className="cf-checkbox-title">🐳 Cài đặt Docker Engine & Docker Compose V2</span>
                    <span className="cf-checkbox-desc">Dành cho VPS trắng chưa cài đặt runtime container</span>
                  </div>
                </label>

                <label className="cf-checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.installNginx}
                    onChange={(e) => updateField('installNginx', e.target.checked)}
                  />
                  <div className="cf-checkbox-text">
                    <span className="cf-checkbox-title">🌐 Cài đặt Nginx Host & Nạp File Config Reverse Proxy</span>
                    <span className="cf-checkbox-desc">Tự động sao chép nginx.conf vào /etc/nginx/sites-available</span>
                  </div>
                </label>

                <label className="cf-checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.installCertbot}
                    onChange={(e) => updateField('installCertbot', e.target.checked)}
                  />
                  <div className="cf-checkbox-text">
                    <span className="cf-checkbox-title">🔒 Cài đặt Chứng chỉ SSL miễn phí (Let's Encrypt / Certbot)</span>
                    <span className="cf-checkbox-desc">Tự động cấu hình HTTPS và cơ chế tự động gia hạn chứng chỉ</span>
                  </div>
                </label>

                <label className="cf-checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.setupFirewall}
                    onChange={(e) => updateField('setupFirewall', e.target.checked)}
                  />
                  <div className="cf-checkbox-text">
                    <span className="cf-checkbox-title">🛡️ Kích hoạt UFW Firewall (Mở cổng 80, 443, 22)</span>
                    <span className="cf-checkbox-desc">Bảo vệ VPS chống quét cổng độc hại</span>
                  </div>
                </label>
              </div>

              {config.installCertbot && (
                <>
                  <div className="cf-field cf-field--toggle cf-field--full" style={{ marginTop: 'var(--space-2)' }}>
                    <div>
                      <span className="cf-toggle-label">🌍 Dùng sslip.io — Auto-Domain từ IP VPS</span>
                      <span className="cf-toggle-desc">Tự động tạo domain HTTPS dạng <code>YOUR_IP.sslip.io</code> không cần mua domain</span>
                    </div>
                    <button
                      id="useSslipIo"
                      type="button"
                      role="switch"
                      aria-checked={config.useSslipIo}
                      className={`cf-toggle ${config.useSslipIo ? 'cf-toggle--on' : ''}`}
                      onClick={() => updateField('useSslipIo', !config.useSslipIo)}
                    >
                      <span className="cf-toggle__thumb" />
                    </button>
                  </div>

                  {config.useSslipIo ? (
                    <div className="cf-sslip-info cf-field--full">
                      <p>
                        ✅ Hệ thống sẽ tự động detect IP VPS và tạo domain dạng <code>YOUR_IP.sslip.io</code>. 
                        Không cần mua tên miền hay cấu hình bản ghi DNS thủ công.
                      </p>
                    </div>
                  ) : (
                    <div className="cf-field cf-field--full">
                      <label htmlFor="setupDomain">Tên miền (Domain Name)</label>
                      <div className="cf-input-wrapper">
                        <input
                          id="setupDomain"
                          type="text"
                          value={config.domainName || ''}
                          placeholder="e.g. api.yourdomain.com"
                          onChange={(e) => updateField('domainName', e.target.value)}
                        />
                      </div>
                    </div>
                  )}

                  {/* Advanced: Email Admin SSL */}
                  <div className="cf-field--full">
                    <button
                      type="button"
                      className="cf-advanced-toggle"
                      onClick={() => setShowAdvancedSSL(!showAdvancedSSL)}
                    >
                      <Settings size={13} />
                      <span>Tùy chọn nâng cao SSL</span>
                      <ChevronDown size={13} className={showAdvancedSSL ? 'cf-chevron--open' : ''} />
                    </button>

                    {showAdvancedSSL && (
                      <div className="cf-field" style={{ marginTop: '10px' }}>
                        <label htmlFor="adminEmail">Email Admin SSL (nhận thông báo gia hạn chứng chỉ)</label>
                        <div className="cf-input-wrapper">
                          <input
                            id="adminEmail"
                            type="email"
                            value={config.adminEmail || ''}
                            placeholder="admin@example.com"
                            onChange={(e) => updateField('adminEmail', e.target.value)}
                          />
                        </div>
                      </div>
                    )}
                  </div>
                </>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default ConfigForm;
