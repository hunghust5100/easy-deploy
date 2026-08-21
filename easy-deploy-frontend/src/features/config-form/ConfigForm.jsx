import { useState } from 'react';
import { useConfig } from '../../context/ConfigContext';
import ServiceSelector from './ServiceSelector';
import {
  AppWindow,
  Database,
  Globe,
  ShieldCheck,
  ChevronDown,
  Settings,
  Container,
  CheckCircle,
} from 'lucide-react';
import './ConfigForm.css';

function ConfigForm() {
  const { config, updateField, enums } = useConfig();
  const [showAdvancedSSL, setShowAdvancedSSL] = useState(false);

  const techStacks = enums?.techStacks || [];
  const dbTypes = enums?.dbTypes || [];
  const deployModes = enums?.deployModes || [];

  return (
    <div className="config-form">
      {/* ── 0. Multi-Service / Module Selector ── */}
      <ServiceSelector />

      {/* ── 1. App Info Card ── */}
      <div className="cf-card">
        <div className="cf-card__header">
          <div className="cf-card__icon-wrap">
            <AppWindow size={16} />
          </div>
          <div className="cf-card__title-group">
            <h3 className="cf-card__title">1. Thông Tin Ứng Dụng</h3>
          </div>
        </div>

        <div className="cf-grid">
          <div className="cf-field">
            <label htmlFor="appName">Tên ứng dụng</label>
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
            <label htmlFor="techStack">Nền tảng công nghệ</label>
            <div className="cf-input-wrapper">
              <select
                id="techStack"
                value={config.techStack}
                onChange={(e) => {
                  const val = e.target.value;
                  updateField('techStack', val);
                  const selected = techStacks.find((t) => t.value === val);
                  if (selected) {
                    if (selected.defaultVersion) updateField('techVersion', selected.defaultVersion);
                    if (selected.defaultPort) updateField('appPort', selected.defaultPort);
                  }
                }}
              >
                {techStacks.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="cf-field">
            <label htmlFor="techVersion">Phiên bản</label>
            <div className="cf-input-wrapper">
              <input
                id="techVersion"
                type="text"
                value={config.techVersion}
                onChange={(e) => updateField('techVersion', e.target.value)}
                placeholder="21, 20, 3.11..."
              />
            </div>
          </div>

          <div className="cf-field">
            <label htmlFor="appPort">Cổng container</label>
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
            <label htmlFor="hostPort">Cổng VPS</label>
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
            <h3 className="cf-card__title">2. Chiến Lược Triển Khai</h3>
          </div>
        </div>

        <div className="cf-strategy-selector">
          {deployModes.map((mode) => {
            const isSelected = (config.deployMode || 'REMOTE_BUILD').toUpperCase() === mode.value.toUpperCase();
            return (
              <div
                key={mode.value}
                className={`cf-strategy-card ${isSelected ? 'cf-strategy-card--active' : ''}`}
                onClick={() => {
                  updateField('deployMode', mode.value);
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
              </div>
            );
          })}
        </div>

        {((config.deployMode || '').toUpperCase() === 'REGISTRY_PULL') && (
          <div className="cf-grid cf-registry-box">
            <div className="cf-field">
              <label htmlFor="dockerHubUsername">Docker Hub Username *</label>
              <div className="cf-input-wrapper">
                <input
                  id="dockerHubUsername"
                  type="text"
                  value={config.dockerHubUsername || ''}
                  placeholder="username"
                  onChange={(e) => {
                    updateField('dockerHubUsername', e.target.value);
                    updateField('dockerHubUser', e.target.value);
                  }}
                />
              </div>
            </div>

            <div className="cf-field">
              <label htmlFor="dockerImageTag">Image Tag</label>
              <div className="cf-input-wrapper">
                <input
                  id="dockerImageTag"
                  type="text"
                  value={config.dockerImageTag || 'latest'}
                  placeholder="latest, v1.0.0"
                  onChange={(e) => updateField('dockerImageTag', e.target.value)}
                />
              </div>
            </div>

            <div className="cf-field">
              <label htmlFor="dockerHubToken">Access Token / Password</label>
              <div className="cf-input-wrapper">
                <input
                  id="dockerHubToken"
                  type="password"
                  value={config.dockerHubToken || ''}
                  placeholder="••••••••••••"
                  onChange={(e) => updateField('dockerHubToken', e.target.value)}
                />
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
            <h3 className="cf-card__title">3. Cơ Sở Dữ Liệu</h3>
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
                {dbTypes.map((d) => (
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
                <label htmlFor="dbUser">Tài khoản DB</label>
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
                <label htmlFor="dbPass">Mật khẩu DB</label>
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
                <label htmlFor="dbPort">Cổng CSDL</label>
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
          </div>
        </div>

        <div className="cf-grid">
          <div className="cf-field cf-field--toggle">
            <div>
              <span className="cf-toggle-label">Bật Nginx Reverse Proxy</span>
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
              <label htmlFor="domainName">Tên miền (Domain)</label>
              <div className="cf-input-wrapper">
                <input
                  id="domainName"
                  type="text"
                  value={config.domainName}
                  placeholder="localhost hoặc domain.com"
                  onChange={(e) => updateField('domainName', e.target.value)}
                />
              </div>
            </div>
          )}
        </div>
      </div>

      {/* ── 5. Bootstrap Server Script & SSL Card ── */}
      <div className="cf-card">
        <div className="cf-card__header">
          <div className="cf-card__icon-wrap">
            <ShieldCheck size={16} />
          </div>
          <div className="cf-card__title-group">
            <h3 className="cf-card__title">5. Cài Đặt Hạ Tầng VPS</h3>
          </div>
        </div>

        <div className="cf-grid">
          <div className="cf-field cf-field--toggle cf-field--full">
            <div>
              <span className="cf-toggle-label">Sinh script cài đặt server (setup-server.sh)</span>
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
                    <span className="cf-checkbox-title">🐳 Cài đặt Docker & Compose V2</span>
                  </div>
                </label>

                <label className="cf-checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.installNginx}
                    onChange={(e) => updateField('installNginx', e.target.checked)}
                  />
                  <div className="cf-checkbox-text">
                    <span className="cf-checkbox-title">🌐 Cài đặt Nginx Host</span>
                  </div>
                </label>

                <label className="cf-checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.installCertbot}
                    onChange={(e) => updateField('installCertbot', e.target.checked)}
                  />
                  <div className="cf-checkbox-text">
                    <span className="cf-checkbox-title">🔒 Cài đặt SSL Let's Encrypt / Certbot</span>
                  </div>
                </label>

                <label className="cf-checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.setupFirewall}
                    onChange={(e) => updateField('setupFirewall', e.target.checked)}
                  />
                  <div className="cf-checkbox-text">
                    <span className="cf-checkbox-title">🛡️ Bật UFW Firewall (80, 443, 22)</span>
                  </div>
                </label>
              </div>

              {config.installCertbot && (
                <>
                  <div className="cf-field cf-field--toggle cf-field--full" style={{ marginTop: 'var(--space-2)' }}>
                    <div>
                      <span className="cf-toggle-label">Tự động tạo tên miền từ IP (sslip.io)</span>
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

                  {!config.useSslipIo && (
                    <div className="cf-field cf-field--full">
                      <label htmlFor="setupDomain">Tên miền</label>
                      <div className="cf-input-wrapper">
                        <input
                          id="setupDomain"
                          type="text"
                          value={config.domainName || ''}
                          placeholder="api.yourdomain.com"
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
                      <span>Email quản trị SSL</span>
                      <ChevronDown size={13} className={showAdvancedSSL ? 'cf-chevron--open' : ''} />
                    </button>

                    {showAdvancedSSL && (
                      <div className="cf-field" style={{ marginTop: '10px' }}>
                        <label htmlFor="adminEmail">Email nhận thông báo gia hạn</label>
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
