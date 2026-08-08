import { useState } from 'react';
import { useConfig } from '../../context/ConfigContext';
import { TECH_STACKS, DB_TYPES } from '../../constants/options';
import {
  AppWindow,
  Database,
  Globe,
  GitBranch,
  Server,
  ShieldCheck,
  ChevronDown,
  Settings,
} from 'lucide-react';
import './ConfigForm.css';

function ConfigForm() {
  const { config, updateField } = useConfig();
  const [showAdvancedSSL, setShowAdvancedSSL] = useState(false);

  return (
    <div className="config-form">
      {/* ── App Info ── */}
      <fieldset className="cf-section">
        <legend className="cf-section__legend">
          <AppWindow size={15} /> Thông tin Ứng dụng
        </legend>
        <div className="cf-grid">
          <div className="cf-field">
            <label htmlFor="appName">Tên ứng dụng</label>
            <input
              id="appName"
              type="text"
              value={config.appName}
              onChange={(e) => updateField('appName', e.target.value)}
            />
          </div>
          <div className="cf-field">
            <label htmlFor="techStack">Tech Stack</label>
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
          <div className="cf-field">
            <label htmlFor="techVersion">Phiên bản</label>
            <input
              id="techVersion"
              type="text"
              value={config.techVersion}
              onChange={(e) => updateField('techVersion', e.target.value)}
            />
          </div>
          <div className="cf-field">
            <label htmlFor="appPort">App Port</label>
            <input
              id="appPort"
              type="number"
              value={config.appPort}
              onChange={(e) => updateField('appPort', parseInt(e.target.value) || 0)}
            />
          </div>
          <div className="cf-field">
            <label htmlFor="hostPort">Host Port</label>
            <input
              id="hostPort"
              type="number"
              value={config.hostPort}
              onChange={(e) => updateField('hostPort', parseInt(e.target.value) || 0)}
            />
          </div>
        </div>
      </fieldset>

      {/* ── Database ── */}
      <fieldset className="cf-section">
        <legend className="cf-section__legend">
          <Database size={15} /> Cơ sở dữ liệu
        </legend>
        <div className="cf-grid">
          <div className="cf-field">
            <label htmlFor="dbType">Loại CSDL</label>
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

          {config.dbType !== 'NONE' && (
            <>
              <div className="cf-field">
                <label htmlFor="dbName">Tên Database</label>
                <input
                  id="dbName"
                  type="text"
                  value={config.dbName}
                  onChange={(e) => updateField('dbName', e.target.value)}
                />
              </div>
              <div className="cf-field">
                <label htmlFor="dbUser">Username</label>
                <input
                  id="dbUser"
                  type="text"
                  value={config.dbUser}
                  onChange={(e) => updateField('dbUser', e.target.value)}
                />
              </div>
              <div className="cf-field">
                <label htmlFor="dbPass">Password</label>
                <input
                  id="dbPass"
                  type="password"
                  value={config.dbPass}
                  onChange={(e) => updateField('dbPass', e.target.value)}
                />
              </div>
              <div className="cf-field">
                <label htmlFor="dbPort">DB Port</label>
                <input
                  id="dbPort"
                  type="number"
                  value={config.dbPort}
                  onChange={(e) => updateField('dbPort', parseInt(e.target.value) || 0)}
                />
              </div>
            </>
          )}
        </div>
      </fieldset>

      {/* ── Nginx ── */}
      <fieldset className="cf-section">
        <legend className="cf-section__legend">
          <Globe size={15} /> Nginx Reverse Proxy
        </legend>
        <div className="cf-grid">
          <div className="cf-field cf-field--toggle">
            <label htmlFor="enableNginx">Bật Nginx</label>
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
              <input
                id="domainName"
                type="text"
                value={config.domainName}
                placeholder="e.g. localhost hoặc api.yourdomain.com"
                onChange={(e) => updateField('domainName', e.target.value)}
              />
              <p className="cf-field-hint">
                🌐 Tên miền để Nginx lắng nghe và chuyển tiếp request (directive <code>server_name</code>).<br />
                💡 <strong>Lưu ý về sslip.io:</strong> Nếu bạn bật tùy chọn <strong>sslip.io</strong> ở phần Setup Server bên dưới, hệ thống sẽ tự động tạo domain <code>IP_VPS.sslip.io</code> cho Nginx và Certbot mà bạn không cần phải mua hay trỏ tên miền thủ công.
              </p>
            </div>
          )}
        </div>
      </fieldset>

      {/* ── CI/CD ── */}
      <fieldset className="cf-section">
        <legend className="cf-section__legend">
          <GitBranch size={15} /> CI/CD Pipeline (GitHub Actions)
        </legend>
        <div className="cf-grid">
          <div className="cf-field cf-field--toggle">
            <label htmlFor="enableCicd">Bật GitHub Actions</label>
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
                <input
                  id="gitBranch"
                  type="text"
                  value={config.gitBranch || 'main'}
                  placeholder="e.g. main, master"
                  onChange={(e) => updateField('gitBranch', e.target.value)}
                />
              </div>

              <div className="cf-field">
                <label htmlFor="dockerHubUser">Docker Hub Username</label>
                <input
                  id="dockerHubUser"
                  type="text"
                  value={config.dockerHubUser || ''}
                  placeholder="Username Docker Hub"
                  onChange={(e) => updateField('dockerHubUser', e.target.value)}
                />
              </div>

              <div className="cf-field">
                <label htmlFor="deployPath">Thư mục Deploy trên VPS</label>
                <input
                  id="deployPath"
                  type="text"
                  value={config.deployPath || ''}
                  placeholder="e.g. /root/my-app"
                  onChange={(e) => updateField('deployPath', e.target.value)}
                />
              </div>
            </>
          )}
        </div>
      </fieldset>

      {/* ── Docker Hub & Compose Deployment Mode ── */}
      <fieldset className="cf-section">
        <legend className="cf-section__legend">
          <Server size={15} /> Docker Hub Registry & Chế độ Deploy
        </legend>
        <div className="cf-grid">
          <div className="cf-field">
            <label htmlFor="deployMode">Chế độ Khởi chạy Docker Compose</label>
            <select
              id="deployMode"
              value={config.deployMode || 'remote_build'}
              onChange={(e) => updateField('deployMode', e.target.value)}
            >
              <option value="remote_build">Build trực tiếp trên VPS (docker compose up --build)</option>
              <option value="registry_pull">Pull Pre-built Image từ Docker Hub (docker compose pull)</option>
            </select>
          </div>

          <div className="cf-field cf-field--toggle">
            <label htmlFor="useDockerHub">Đăng nhập & Pull từ Docker Hub</label>
            <button
              id="useDockerHub"
              type="button"
              role="switch"
              aria-checked={config.useDockerHub}
              className={`cf-toggle ${config.useDockerHub ? 'cf-toggle--on' : ''}`}
              onClick={() => updateField('useDockerHub', !config.useDockerHub)}
            >
              <span className="cf-toggle__thumb" />
            </button>
          </div>

          {config.useDockerHub && (
            <>
              <div className="cf-field">
                <label htmlFor="dockerHubUsername">Username Docker Hub</label>
                <input
                  id="dockerHubUsername"
                  type="text"
                  value={config.dockerHubUsername || ''}
                  placeholder="dockerhub_username"
                  onChange={(e) => updateField('dockerHubUsername', e.target.value)}
                />
              </div>

              <div className="cf-field">
                <label htmlFor="dockerHubToken">Access Token / Password</label>
                <input
                  id="dockerHubToken"
                  type="password"
                  value={config.dockerHubToken || ''}
                  placeholder="dckr_pat_xxxxx"
                  onChange={(e) => updateField('dockerHubToken', e.target.value)}
                />
              </div>

              <div className="cf-field cf-field--full">
                <label htmlFor="dockerImageTag">Docker Image Tag Target</label>
                <input
                  id="dockerImageTag"
                  type="text"
                  value={config.dockerImageTag || ''}
                  placeholder="e.g. myusername/my-app:latest"
                  onChange={(e) => updateField('dockerImageTag', e.target.value)}
                />
              </div>
            </>
          )}
        </div>
      </fieldset>



      {/* ── Server Setup Options & Free SSL ── */}
      <fieldset className="cf-section">
        <legend className="cf-section__legend">
          <ShieldCheck size={15} /> Tùy chọn Setup Server & Bảo mật SSL Miễn phí
        </legend>
        
        <div className="cf-grid">
          <div className="cf-field cf-field--toggle cf-field--full">
            <label htmlFor="enableServerSetup">Bật Setup Server Tự động (Server Provisioning)</label>
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
                  <span>🐳 Cài đặt Docker Engine & Docker Compose V2 (Dành cho VPS mới)</span>
                </label>

                <label className="cf-checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.installNginx}
                    onChange={(e) => updateField('installNginx', e.target.checked)}
                  />
                  <span>🌐 Cài đặt Nginx Host & Nạp File Config Reverse Proxy</span>
                </label>

                <label className="cf-checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.installCertbot}
                    onChange={(e) => updateField('installCertbot', e.target.checked)}
                  />
                  <span>🔒 Cài đặt Chứng chỉ SSL miễn phí (Let's Encrypt / Certbot)</span>
                </label>

                <label className="cf-checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.setupFirewall}
                    onChange={(e) => updateField('setupFirewall', e.target.checked)}
                  />
                  <span>🛡️ Kích hoạt UFW Firewall (Mở cổng 80, 443, 22)</span>
                </label>
              </div>

              {config.installCertbot && (
                <>
                  <div className="cf-field cf-field--toggle cf-field--full" style={{ marginTop: 'var(--space-2)' }}>
                    <label htmlFor="useSslipIo">
                      🌍 Dùng <strong>sslip.io</strong> — tự động tạo domain từ IP VPS (không cần mua domain)
                    </label>
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
                        Không cần mua tên miền hay trỏ DNS.
                      </p>
                    </div>
                  ) : (
                    <div className="cf-field cf-field--full">
                      <label htmlFor="setupDomain">Tên miền (Domain Name)</label>
                      <input
                        id="setupDomain"
                        type="text"
                        value={config.domainName || ''}
                        placeholder="e.g. api.yourdomain.com"
                        onChange={(e) => updateField('domainName', e.target.value)}
                      />
                    </div>
                  )}

                  {/* Advanced: Email Admin SSL */}
                  <div className="cf-field--full">
                    <button
                      type="button"
                      className="cf-advanced-toggle"
                      onClick={() => setShowAdvancedSSL(!showAdvancedSSL)}
                    >
                      <Settings size={12} />
                      <span>Tùy chọn nâng cao</span>
                      <ChevronDown size={12} className={showAdvancedSSL ? 'cf-chevron--open' : ''} />
                    </button>

                    {showAdvancedSSL && (
                      <div className="cf-field" style={{ marginTop: '8px' }}>
                        <label htmlFor="adminEmail">Email Admin SSL (nhận thông báo gia hạn chứng chỉ)</label>
                        <input
                          id="adminEmail"
                          type="email"
                          value={config.adminEmail || ''}
                          placeholder="admin@example.com (mặc định)"
                          onChange={(e) => updateField('adminEmail', e.target.value)}
                        />
                      </div>
                    )}
                  </div>
                </>
              )}
            </>
          )}
        </div>
      </fieldset>
    </div>
  );
}

export default ConfigForm;

