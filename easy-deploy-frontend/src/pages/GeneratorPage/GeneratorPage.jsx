import { useState, useEffect } from 'react';
import GithubUrlInput from '../../features/github-analyzer/GithubUrlInput';
import ConfigForm from '../../features/config-form/ConfigForm';
import PreviewPanel from '../../features/preview-export/PreviewPanel';
import DeployLogViewer from '../../features/deploy/DeployLogViewer';
import ManualDeployModal from '../../features/deploy/ManualDeployModal';
import { useConfig } from '../../context/ConfigContext';
import { useVps } from '../../context/VpsContext';
import {
  Rocket,
  HelpCircle,
  Server,
  Hash,
  User,
  Lock,
  Folder,
  Terminal,
  ChevronDown,
  ChevronUp,
  Copy,
  Check,
  BookmarkPlus,
  Trash2,
  Key,
  Container,
  Code2,
} from 'lucide-react';
import './GeneratorPage.css';

function GeneratorPage() {
  const { config } = useConfig();
  const { vpsList, saveVpsProfile, deleteVpsProfile } = useVps();
  const [showManualModal, setShowManualModal] = useState(false);
  const [showScriptPreview, setShowScriptPreview] = useState(true);
  const [copiedScript, setCopiedScript] = useState(false);
  const [authMethod, setAuthMethod] = useState('password'); // 'password' | 'key'
  const [selectedVpsId, setSelectedVpsId] = useState('');
  const [cleanServerBeforeDeploy, setCleanServerBeforeDeploy] = useState(false);

  const appName = config.appName || 'my-app';

  const [vpsForm, setVpsForm] = useState({
    host: '',
    port: 22,
    username: 'root',
    password: '',
    keyFilePath: '~/.ssh/id_rsa',
    deployPath: `/root/${appName}`,
  });

  // Sync deployPath when config.appName or config.deployPath updates from GitHub Scan
  useEffect(() => {
    if (config.appName) {
      setVpsForm((prev) => {
        if (!prev.deployPath || prev.deployPath.startsWith('/root/')) {
          return { ...prev, deployPath: config.deployPath || `/root/${config.appName}` };
        }
        return prev;
      });
    }
  }, [config.appName, config.deployPath]);

  const handleVpsChange = (e) => {
    const { name, value } = e.target;
    setVpsForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSelectVps = (e) => {
    const id = e.target.value;
    setSelectedVpsId(id);
    if (!id) return;

    const selected = vpsList.find((v) => v.id === id);
    if (selected) {
      setVpsForm({
        host: selected.host || '',
        port: selected.port || 22,
        username: selected.username || 'root',
        password: selected.password || '',
        keyFilePath: selected.keyFilePath || '~/.ssh/id_rsa',
        deployPath: selected.deployPath || `/root/${appName}`,
      });

      if (selected.authMethod) {
        setAuthMethod(selected.authMethod);
      } else if (selected.keyFilePath && !selected.password) {
        setAuthMethod('key');
      } else {
        setAuthMethod('password');
      }
    }
  };

  const handleSaveVps = () => {
    if (!vpsForm.host || !vpsForm.host.trim()) {
      alert('Vui lòng nhập Host / IP của máy chủ VPS trước khi lưu!');
      return;
    }

    const defaultName = selectedVpsId
      ? vpsList.find((v) => v.id === selectedVpsId)?.name
      : `${vpsForm.username || 'root'}@${vpsForm.host.trim()}`;

    const name = prompt(
      'Nhập tên gợi nhớ cho máy chủ VPS này (VD: Production AWS, VPS Vietnix):',
      defaultName || `${vpsForm.username || 'root'}@${vpsForm.host.trim()}`
    );

    if (name && name.trim()) {
      const savedId = selectedVpsId || `vps-${Date.now()}`;
      saveVpsProfile({
        id: savedId,
        name: name.trim(),
        host: vpsForm.host.trim(),
        port: parseInt(vpsForm.port) || 22,
        username: (vpsForm.username || 'root').trim(),
        password: vpsForm.password || '',
        authMethod: authMethod,
        keyFilePath: authMethod === 'key' ? (vpsForm.keyFilePath || '~/.ssh/id_rsa') : '',
        deployPath: vpsForm.deployPath || `/root/${appName}`,
      });
      setSelectedVpsId(savedId);
      alert(`Đã lưu máy chủ "${name.trim()}" (${vpsForm.host.trim()}) vào danh sách!`);
    }
  };

  const handleDeleteVps = () => {
    if (!selectedVpsId) return;
    const vps = vpsList.find((v) => v.id === selectedVpsId);
    if (confirm(`Bạn có chắc muốn xoá máy chủ "${vps?.name || selectedVpsId}" khỏi danh sách?`)) {
      deleteVpsProfile(selectedVpsId);
      setSelectedVpsId('');
    }
  };

  const targetPath = vpsForm.deployPath || `/root/${appName}`;

  // Dynamically build script preview steps
  const scriptSteps = [];
  scriptSteps.push(`# 1. Khởi tạo kết nối SSH & SFTP tới máy chủ VPS từ xa (Auth: ${authMethod === 'key' ? 'SSH Key ' + vpsForm.keyFilePath : 'Password'})`);
  scriptSteps.push(`ssh ${authMethod === 'key' ? '-i ' + vpsForm.keyFilePath + ' ' : ''}-p ${vpsForm.port || 22} ${vpsForm.username || 'root'}@${vpsForm.host || '127.0.0.1'}`);
  
  scriptSteps.push(`\n# 2. Tạo thư mục làm việc và Upload bộ file cấu hình`);
  scriptSteps.push(`mkdir -p ${targetPath}`);
  scriptSteps.push(`sftp upload: (Dockerfile, docker-compose.yml, .env, .env.example, nginx.conf, .dockerignore${config.enableServerSetup ? ', setup-server.sh' : ''}) -> ${targetPath}/`);

  if (config.enableServerSetup) {
    const sslNote = config.useSslipIo ? ' + sslip.io Auto-Domain' : '';
    scriptSteps.push(`\n# 3. Kích chạy Setup Server Tự động (Docker, Nginx, SSL${sslNote}, UFW Firewall)`);
    scriptSteps.push(`cd ${targetPath} && chmod +x setup-server.sh && ./setup-server.sh`);
    if (config.useSslipIo) {
      scriptSteps.push(`# → Domain tự động: $(curl -4s ifconfig.me).sslip.io`);
    }
  }

  scriptSteps.push(`\n# ${config.enableServerSetup ? '4' : '3'}. Khởi chạy Docker Compose App Containers (Mode: ${config.deployMode || 'remote_build'})`);
  if (config.deployMode === 'registry_pull') {
    if (config.useDockerHub && config.dockerHubUsername) {
      scriptSteps.push(`echo "${config.dockerHubToken || '••••••••'}" | docker login -u "${config.dockerHubUsername}" --password-stdin`);
    }
    const fullImg = `${config.dockerHubUsername || 'username'}/${appName}:${config.dockerImageTag || 'latest'}`;
    scriptSteps.push(`docker pull ${fullImg}`);
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
        <span>Hoặc Tự Cấu Hình Bên Dưới</span>
      </div>

      {/* ── Step 2: Config Form ── */}
      <section className="gp-section">
        <ConfigForm />
      </section>

      {/* ── Step 3: Preview & Download ── */}
      <section id="preview-section" className="gp-section">
        <div className="gp-section__header-card">
          <div className="gp-section__header-left">
            <div className="gp-section__icon-badge">
              <Code2 size={16} />
            </div>
            <div>
              <h3 className="gp-section__title">3. Bộ Tệp Tin DevOps Đã Sinh</h3>
              <p className="gp-section__subtitle">Xem trước, chỉnh sửa trực tiếp và xuất file .zip hoàn chỉnh.</p>
            </div>
          </div>

          <button
            type="button"
            className="gp-manual-btn"
            onClick={() => setShowManualModal(true)}
          >
            <HelpCircle size={15} />
            <span>Hướng dẫn Cấu hình Bắt buộc (Vercel, Secrets, VPS)</span>
          </button>
        </div>

        <PreviewPanel />
      </section>

      {/* ── Step 4: 1-Click Deploy ── */}
      <section className="gp-section gp-section--deploy">
        <div className="gp-deploy-header">
          <div className="gp-deploy-header__left">
            <div className="gp-deploy-icon-wrap">
              <Rocket size={18} />
            </div>
            <div>
              <h3 className="gp-deploy-title">4. Triển Khai 1-Click Lên Máy Chủ VPS (SSH Deploy)</h3>
              <p className="gp-deploy-desc">
                Tự động kết nối SSH/SFTP, đẩy file cấu hình lên máy chủ và khởi chạy container trong tích tắc.
              </p>
            </div>
          </div>

          <div className="gp-deploy-mode-badge">
            <Container size={13} />
            <span>
              Chế độ: <strong>{config.deployMode === 'registry_pull' ? 'Docker Hub Pull' : 'Remote Build'}</strong>
            </span>
          </div>
        </div>

        {/* VPS Connection Box */}
        <div className="gp-vps-card">
          <div className="gp-vps-card__header">
            <div className="gp-vps-header-left">
              <Server size={15} />
              <span>Thông tin Kết nối SSH Máy chủ (VPS Credentials)</span>
            </div>

            {/* Quick selector */}
            <div className="gp-vps-select-wrap">
              <select
                className="gp-vps-select"
                value={selectedVpsId}
                onChange={handleSelectVps}
              >
                <option value="">⚡ Chọn máy chủ đã lưu ({vpsList.length} VPS)</option>
                {vpsList.map((v) => (
                  <option key={v.id} value={v.id}>
                    🖥️ {v.name} ({v.username}@{v.host}:{v.port})
                  </option>
                ))}
              </select>

              <button
                type="button"
                className="gp-save-vps-btn"
                title="Lưu cấu hình VPS hiện tại"
                onClick={handleSaveVps}
              >
                <BookmarkPlus size={13} /> Lưu VPS
              </button>

              {selectedVpsId && (
                <button
                  type="button"
                  className="gp-save-vps-btn gp-save-vps-btn--danger"
                  title="Xoá VPS đang chọn khỏi danh sách"
                  onClick={handleDeleteVps}
                >
                  <Trash2 size={13} />
                </button>
              )}
            </div>
          </div>

          <div className="gp-vps-grid">
            {/* Host IP */}
            <div className="gp-input-group">
              <label>Host / IP Máy chủ VPS *</label>
              <div className="gp-input-wrapper">
                <Server size={14} className="gp-input-icon" />
                <input
                  type="text"
                  name="host"
                  placeholder="Ví dụ: 103.179.188.25"
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
              <label>Username SSH *</label>
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

            {/* Auth Method Selector */}
            <div className="gp-input-group">
              <label>Phương thức Xác thực</label>
              <div className="gp-auth-toggle">
                <button
                  type="button"
                  className={`gp-auth-btn ${authMethod === 'password' ? 'active' : ''}`}
                  onClick={() => setAuthMethod('password')}
                >
                  <Lock size={12} /> Password
                </button>
                <button
                  type="button"
                  className={`gp-auth-btn ${authMethod === 'key' ? 'active' : ''}`}
                  onClick={() => setAuthMethod('key')}
                >
                  <Key size={12} /> SSH Key
                </button>
              </div>
            </div>

            {/* Password or Key file path input */}
            {authMethod === 'password' ? (
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
            ) : (
              <div className="gp-input-group">
                <label>Đường dẫn SSH Private Key</label>
                <div className="gp-input-wrapper">
                  <Key size={14} className="gp-input-icon" />
                  <input
                    type="text"
                    name="keyFilePath"
                    placeholder="~/.ssh/id_rsa"
                    value={vpsForm.keyFilePath}
                    onChange={handleVpsChange}
                  />
                </div>
              </div>
            )}

            {/* Deploy Path */}
            <div className="gp-input-group">
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

            {/* Clean Server Option */}
            <div className="gp-input-group gp-input-group--full">
              <label className="gp-checkbox-label">
                <input
                  type="checkbox"
                  checked={cleanServerBeforeDeploy}
                  onChange={(e) => setCleanServerBeforeDeploy(e.target.checked)}
                />
                <span>🧹 Dọn sạch server trước khi triển khai (xoá container, image cũ & thư mục deploy)</span>
              </label>
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
                <span>Xem các câu lệnh Bash SSH sẽ tự động thực thi trên VPS</span>
              </div>
              <button type="button" className="gp-script-toggle">
                {showScriptPreview ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
              </button>
            </div>

            {showScriptPreview && (
              <div className="gp-script-box">
                <div className="gp-script-box__top">
                  <span>SSH Automated Script</span>
                  <button type="button" className="gp-copy-script-btn" onClick={handleCopyScript}>
                    {copiedScript ? <Check size={13} className="text-success" /> : <Copy size={13} />}
                    <span>{copiedScript ? 'Đã sao chép' : 'Sao chép Script'}</span>
                  </button>
                </div>
                <pre className="gp-script-code">{generatedScript}</pre>
              </div>
            )}
          </div>
        </div>

        {/* Live Deploy Terminal Logs */}
        <DeployLogViewer
          config={config}
          credentials={{
            ...vpsForm,
            keyFilePath: authMethod === 'key' ? vpsForm.keyFilePath : null,
            cleanServerBeforeDeploy,
          }}
        />
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
