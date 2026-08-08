import { useState } from 'react';
import GithubUrlInput from '../../features/github-analyzer/GithubUrlInput';
import ConfigForm from '../../features/config-form/ConfigForm';
import PreviewPanel from '../../features/preview-export/PreviewPanel';
import DeployLogViewer from '../../features/deploy/DeployLogViewer';
import ManualDeployModal from '../../features/deploy/ManualDeployModal';
import { useConfig } from '../../context/ConfigContext';
import { useVps } from '../../context/VpsContext';
import { Rocket, HelpCircle, Server, Hash, User, Lock, Folder, Terminal, ChevronDown, ChevronUp, Copy, Check, BookmarkPlus } from 'lucide-react';
import './GeneratorPage.css';

function GeneratorPage() {
  const { config } = useConfig();
  const { vpsList, saveVpsProfile } = useVps();
  const [showManualModal, setShowManualModal] = useState(false);
  const [showScriptPreview, setShowScriptPreview] = useState(true);
  const [copiedScript, setCopiedScript] = useState(false);

  const [vpsForm, setVpsForm] = useState({
    host: '',
    port: 22,
    username: '',
    password: '',
    deployPath: `/root/${config.appName || 'my-app'}`,
  });

  const handleVpsChange = (e) => {
    const { name, value } = e.target;
    setVpsForm((prev) => ({ ...prev, [name]: value }));
  };

  const appName = config.appName || 'my-app';
  const targetPath = vpsForm.deployPath || `/root/${appName}`;

  // Dynamically build script preview steps
  const scriptSteps = [];
  scriptSteps.push(`# 1. Khởi tạo kết nối SSH & SFTP tới máy chủ VPS từ xa`);
  scriptSteps.push(`ssh -p ${vpsForm.port || 22} ${vpsForm.username || 'root'}@${vpsForm.host || '127.0.0.1'}`);
  
  scriptSteps.push(`\n# 2. Tạo thư mục làm việc và Upload bộ file cấu hình`);
  scriptSteps.push(`mkdir -p ${targetPath}`);
  scriptSteps.push(`sftp upload: (Dockerfile, docker-compose.yml, nginx.conf, .dockerignore${config.enableServerSetup ? ', setup-server.sh' : ''}) -> ${targetPath}/`);

  if (config.enableServerSetup) {
    const sslNote = config.useSslipIo ? ' + sslip.io Auto-Domain' : '';
    scriptSteps.push(`\n# 3. Kích chạy Setup Server Tự động (Docker, Nginx, SSL${sslNote}, UFW Firewall)`);
    scriptSteps.push(`cd ${targetPath} && chmod +x setup-server.sh && ./setup-server.sh`);
    if (config.useSslipIo) {
      scriptSteps.push(`# → Domain tự động: $(curl -4s ifconfig.me).sslip.io`);
    }
  }

  scriptSteps.push(`\n# ${config.enableServerSetup ? '4' : '3'}. Khởi chạy Docker Compose App Containers`);
  if (config.deployMode === 'registry_pull') {
    if (config.useDockerHub && config.dockerHubUsername) {
      scriptSteps.push(`echo "${config.dockerHubToken || '••••••••'}" | docker login -u "${config.dockerHubUsername}" --password-stdin`);
    }
    if (config.dockerImageTag) {
      scriptSteps.push(`docker pull ${config.dockerImageTag}`);
    }
    scriptSteps.push(`cd ${targetPath} && docker compose pull && docker compose up -d`);
  } else {
    scriptSteps.push(`cd ${targetPath} && docker compose up -d --build`);
  }

  const generatedScript = scriptSteps.join('\n');

  const handleCopyScript = () => {
    navigator.clipboard.writeText(generatedScript);
    setCopiedScript(true);
    setTimeout(() => setCopiedScript(false), 2000);
  };

  return (
    <div className="generator-page">
      {/* ── Step 1: GitHub URL ── */}
      <section className="gp-section">
        <GithubUrlInput />
      </section>

      {/* ── Divider ── */}
      <div className="gp-divider">
        <span>hoặc điền thủ công</span>
      </div>

      {/* ── Step 2: Config Form ── */}
      <section className="gp-section">
        <ConfigForm />
      </section>

      {/* ── Step 3: Preview & Download ── */}
      <section id="preview-section" className="gp-section">
        <div className="gp-section__header">
          <h3 className="gp-section__title">Kết quả & Xem trước</h3>
          <button
            type="button"
            className="gp-manual-btn"
            onClick={() => setShowManualModal(true)}
          >
            <HelpCircle size={14} />
            <span>Hướng dẫn Cấu hình Bắt buộc (Vercel, Secrets, VPS)</span>
          </button>
        </div>
        <PreviewPanel />
      </section>

      {/* ── Step 4: 1-Click Deploy ── */}
      <section className="gp-section gp-section--deploy">
        <div className="gp-section__header">
          <div>
            <h3 className="gp-section__title">
              <Rocket size={18} className="gp-title-icon" />
              <span>Triển khai 1-Click lên VPS (1-Click SSH Deploy)</span>
            </h3>
            <p className="gp-section__subtitle">
              Nhập thông tin SSH máy chủ VPS của bạn. Hệ thống sẽ tự động kết nối SFTP upload file và chạy <code>docker compose up -d</code>.
            </p>
          </div>
        </div>

        {/* Form nhập thông số SSH VPS */}
        <div className="gp-vps-card">
          <div className="gp-vps-card__header">
            <Server size={16} />
            <span>Thông tin Kết nối SSH Máy chủ (VPS Credentials)</span>
          </div>

          <div className="gp-vps-grid">
            {/* Selector chọn VPS từ danh sách đã lưu */}
            <div className="gp-input-group gp-input-group--full">
              <label>⚡ Chọn từ danh sách VPS đang quản lý ({vpsList.length} Server)</label>
              <div className="gp-vps-select-row">
                <select
                  className="gp-vps-select"
                  onChange={(e) => {
                    const selected = vpsList.find((v) => v.id === e.target.value);
                    if (selected) {
                      setVpsForm({
                        host: selected.host || '',
                        port: selected.port || 22,
                        username: selected.username || '',
                        password: selected.password || '',
                        deployPath: selected.deployPath || `/root/${appName}`,
                      });
                    }
                  }}
                >
                  <option value="">-- Chọn Server VPS đã lưu --</option>
                  {vpsList.map((v) => (
                    <option key={v.id} value={v.id}>
                      🖥️ {v.name} ({v.username}@{v.host}:{v.port})
                    </option>
                  ))}
                </select>
                <button
                  type="button"
                  className="gp-save-vps-btn"
                  title="Lưu cấu hình VPS hiện tại vào danh sách quản lý"
                  onClick={() => {
                    const name = prompt('Nhập tên đại diện cho VPS này (VD: Production AWS):', `${vpsForm.username}@${vpsForm.host}`);
                    if (name) {
                      saveVpsProfile({
                        name,
                        host: vpsForm.host,
                        port: vpsForm.port,
                        username: vpsForm.username,
                        password: vpsForm.password,
                        deployPath: vpsForm.deployPath,
                      });
                      alert(`Đã lưu máy chủ "${name}" vào danh sách VPS!`);
                    }
                  }}
                >
                  <BookmarkPlus size={14} /> Lưu VPS này
                </button>
              </div>
            </div>

            {/* Host IP */}
            <div className="gp-input-group">
              <label>Host / IP Máy chủ</label>
              <div className="gp-input-wrapper">
                <Server size={14} className="gp-input-icon" />
                <input
                  type="text"
                  name="host"
                  placeholder="Ví dụ: 103.179.x.x"
                  value={vpsForm.host}
                  onChange={handleVpsChange}
                />
              </div>
            </div>

            {/* Port */}
            <div className="gp-input-group">
              <label>Port SSH</label>
              <div className="gp-input-wrapper">
                <Hash size={14} className="gp-input-icon" />
                <input
                  type="number"
                  name="port"
                  placeholder="22"
                  value={vpsForm.port}
                  onChange={handleVpsChange}
                />
              </div>
            </div>

            {/* Username */}
            <div className="gp-input-group">
              <label>Username SSH</label>
              <div className="gp-input-wrapper">
                <User size={14} className="gp-input-icon" />
                <input
                  type="text"
                  name="username"
                  placeholder="root hoặc deploy"
                  value={vpsForm.username}
                  onChange={handleVpsChange}
                />
              </div>
            </div>

            {/* Password */}
            <div className="gp-input-group">
              <label>Mật khẩu SSH</label>
              <div className="gp-input-wrapper">
                <Lock size={14} className="gp-input-icon" />
                <input
                  type="password"
                  name="password"
                  placeholder="••••••••••••"
                  value={vpsForm.password}
                  onChange={handleVpsChange}
                />
              </div>
            </div>

            {/* Deploy Path */}
            <div className="gp-input-group gp-input-group--full">
              <label>Thư mục Lưu trữ trên VPS</label>
              <div className="gp-input-wrapper">
                <Folder size={14} className="gp-input-icon" />
                <input
                  type="text"
                  name="deployPath"
                  placeholder={`/root/${appName}`}
                  value={vpsForm.deployPath}
                  onChange={handleVpsChange}
                />
              </div>
            </div>
          </div>

          {/* Sub-section: Live Script Preview */}
          <div className="gp-script-preview">
            <div
              className="gp-script-preview__header"
              onClick={() => setShowScriptPreview(!showScriptPreview)}
            >
              <div className="gp-script-preview__title">
                <Terminal size={14} />
                <span>Xem các câu lệnh Script SSH sẽ tự động thực thi trên VPS</span>
              </div>
              <button type="button" className="gp-script-toggle">
                {showScriptPreview ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
              </button>
            </div>

            {showScriptPreview && (
              <div className="gp-script-box">
                <div className="gp-script-box__top">
                  <span>Script Exec Preview</span>
                  <button type="button" className="gp-copy-script-btn" onClick={handleCopyScript}>
                    {copiedScript ? <Check size={13} className="copied" /> : <Copy size={13} />}
                    <span>{copiedScript ? 'Đã sao chép' : 'Sao chép Script'}</span>
                  </button>
                </div>
                <pre className="gp-script-code">{generatedScript}</pre>
              </div>
            )}
          </div>
        </div>

        {/* Live Deploy Terminal Logs */}
        <DeployLogViewer config={config} credentials={vpsForm} />
      </section>

      {/* Modal Hướng dẫn Thủ công */}
      <ManualDeployModal
        isOpen={showManualModal}
        onClose={() => setShowManualModal(false)}
      />
    </div>
  );
}

export default GeneratorPage;

