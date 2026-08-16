import { useEffect, useRef, useState } from 'react';
import { Terminal } from '@xterm/xterm';
import { FitAddon } from '@xterm/addon-fit';
import {
  Rocket,
  Loader2,
  CheckCircle2,
  AlertTriangle,
  ExternalLink,
  Copy,
  Check,
  Globe,
  ShieldCheck,
  Server,
  Settings,
  Key,
  FolderGit2,
  Cpu,
  Layers,
  Terminal as TerminalIcon,
  Maximize2,
  Minimize2,
  Trash2,
} from 'lucide-react';
import '@xterm/xterm/css/xterm.css';
import './DeployLogViewer.css';

const DEPLOY_STEPS = [
  { id: 1, name: 'Chuẩn bị Cấu hình', desc: 'Sinh Dockerfile, Compose, .env', icon: Settings },
  { id: 2, name: 'Kết nối SSH VPS', desc: 'Xác thực & Kết nối an toàn', icon: Key },
  { id: 3, name: 'Đồng bộ Mã Nguồn', desc: 'Git Clone & Nạp .env qua SFTP', icon: FolderGit2 },
  { id: 4, name: 'Cấu hình Hạ Tầng', desc: 'Docker, Firewall, Nginx, SSL', icon: Cpu },
  { id: 5, name: 'Docker Compose', desc: 'Build Image & Start Containers', icon: Layers },
];

function DeployLogViewer({ config, credentials, onFinished }) {
  const terminalRef = useRef(null);
  const termInstanceRef = useRef(null);
  const fitAddonRef = useRef(null);
  const wsRef = useRef(null);

  const [deploying, setDeploying] = useState(false);
  const [status, setStatus] = useState('idle'); // 'idle' | 'running' | 'success' | 'error'
  const [currentStep, setCurrentStep] = useState(0); // 0 = not started, 1..5
  const [stepStatuses, setStepStatuses] = useState({
    1: 'pending',
    2: 'pending',
    3: 'pending',
    4: 'pending',
    5: 'pending',
  });
  const [copiedUrl, setCopiedUrl] = useState(null);
  const [isExpanded, setIsExpanded] = useState(false);

  const hostIp = credentials?.host || '127.0.0.1';
  const port = config?.hostPort || config?.appPort || 8080;
  const directAppUrl = `http://${hostIp}${port === 80 ? '' : `:${port}`}`;
  const nginxDomainUrl = config?.enableNginx && config?.domainName && config.domainName !== 'localhost' ? `http://${config.domainName}` : null;
  const sslAutoDomainUrl = config?.useSslipIo ? `https://${hostIp}.sslip.io` : null;

  const handleCopyLink = (url, key) => {
    navigator.clipboard.writeText(url);
    setCopiedUrl(key);
    setTimeout(() => setCopiedUrl(null), 2000);
  };

  const updateStepProgress = (stepNumber) => {
    setCurrentStep(stepNumber);
    setStepStatuses((prev) => {
      const next = { ...prev };
      for (let i = 1; i < stepNumber; i++) {
        next[i] = 'success';
      }
      next[stepNumber] = 'running';
      return next;
    });
  };

  const start1ClickDeploy = () => {
    if (deploying) return;

    setDeploying(true);
    setStatus('running');
    setCurrentStep(1);
    setStepStatuses({
      1: 'running',
      2: 'pending',
      3: 'pending',
      4: 'pending',
      5: 'pending',
    });

    // 1. Khởi tạo Terminal với convertEol: true (loại bỏ bậc thang)
    if (!termInstanceRef.current && terminalRef.current) {
      const term = new Terminal({
        convertEol: true, // Tự động đổi \n thành \r\n
        cursorBlink: true,
        fontSize: 12.5,
        fontFamily: "'JetBrains Mono', 'Fira Code', Menlo, Consolas, monospace",
        lineHeight: 1.25,
        theme: {
          background: '#0a0e17',
          foreground: '#e2e8f0',
          cursor: '#38bdf8',
          selectionBackground: '#1e293b',
          black: '#0a0e17',
          red: '#f87171',
          green: '#4ade80',
          yellow: '#fbbf24',
          blue: '#60a5fa',
          magenta: '#c084fc',
          cyan: '#38bdf8',
          white: '#f8fafc',
          brightBlack: '#64748b',
          brightRed: '#ef4444',
          brightGreen: '#22c55e',
          brightYellow: '#f59e0b',
          brightBlue: '#3b82f6',
          brightMagenta: '#a855f7',
          brightCyan: '#0ea5e9',
          brightWhite: '#ffffff',
        },
      });

      const fitAddon = new FitAddon();
      term.loadAddon(fitAddon);
      term.open(terminalRef.current);
      fitAddon.fit();

      termInstanceRef.current = term;
      fitAddonRef.current = fitAddon;
    } else if (termInstanceRef.current) {
      termInstanceRef.current.clear();
    }

    const term = termInstanceRef.current;

    // 2. Mở kết nối WebSocket tới /ws/deploy-logs
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.hostname}:8088/ws/deploy-logs`;
    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen = () => {
      ws.send(
        JSON.stringify({
          config,
          credentials,
        })
      );
    };

    ws.onmessage = (event) => {
      const msg = event.data;
      term?.write(msg);

      // Nhận diện Step chuyển giao từ Log Marker
      if (msg.includes('[EZ_STEP:1]')) {
        updateStepProgress(1);
      } else if (msg.includes('[EZ_STEP:2]')) {
        updateStepProgress(2);
      } else if (msg.includes('[EZ_STEP:3]')) {
        updateStepProgress(3);
      } else if (msg.includes('[EZ_STEP:4]')) {
        updateStepProgress(4);
      } else if (msg.includes('[EZ_STEP:5]')) {
        updateStepProgress(5);
      }

      // CHỈ KẾT THÚC THÀNH CÔNG KHI NHẬN ĐƯỢC [EZ_STATUS:SUCCESS] (Toàn bộ container up)
      if (msg.includes('[EZ_STATUS:SUCCESS]')) {
        setStatus('success');
        setDeploying(false);
        setCurrentStep(5);
        setStepStatuses({
          1: 'success',
          2: 'success',
          3: 'success',
          4: 'success',
          5: 'success',
        });
        onFinished?.('success');
      } else if (msg.includes('[EZ_STATUS:ERROR]')) {
        setStatus('error');
        setDeploying(false);
        setStepStatuses((prev) => ({
          ...prev,
          [currentStep || 5]: 'error',
        }));
        onFinished?.('error');
      }
    };

    ws.onerror = () => {
      term?.write('\r\n\u001b[31m[WebSocket Error] Không thể kết nối tới máy chủ EasyDeploy backend (Port 8088).\u001b[0m\r\n');
      setDeploying(false);
      setStatus('error');
      setStepStatuses((prev) => ({
        ...prev,
        [currentStep || 1]: 'error',
      }));
    };

    ws.onclose = () => {
      setDeploying(false);
    };

    const handleResize = () => fitAddonRef.current?.fit();
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
    };
  };

  const handleClearTerminal = () => {
    termInstanceRef.current?.clear();
  };

  useEffect(() => {
    return () => {
      if (wsRef.current) wsRef.current.close();
      if (termInstanceRef.current) termInstanceRef.current.dispose();
    };
  }, []);

  return (
    <div className={`deploy-viewer ${isExpanded ? 'deploy-viewer--expanded' : ''}`}>
      {/* ── Main Launch CTA Button Area ── */}
      <div className="deploy-viewer__launch-area">
        <button
          type="button"
          className={`deploy-viewer__launch-btn ${deploying ? 'deploying' : ''}`}
          onClick={start1ClickDeploy}
          disabled={deploying || !credentials?.host}
        >
          {deploying ? (
            <>
              <Loader2 size={20} className="spin" />
              <span>Đang Thực Thi Triển Khai Tới VPS ({credentials.host})...</span>
            </>
          ) : (
            <>
              <Rocket size={20} />
              <span>Khởi Chạy 1-Click SSH Deploy Ngay</span>
            </>
          )}
        </button>

        {!credentials?.host && (
          <span className="deploy-viewer__hint">
            ⚠️ Hãy nhập Host / IP máy chủ VPS ở trên trước khi khởi chạy.
          </span>
        )}
      </div>

      {/* ── Pipeline Stepper Tracker (Gom Log Theo Từng Bước) ── */}
      <div className="deploy-stepper-card">
        <div className="deploy-stepper__header">
          <span className="deploy-stepper__title">
            <Layers size={15} />
            <span>Tiến Trình Triển Khai Tự Động (Deployment Pipeline)</span>
          </span>
          <span className={`deploy-stepper__status-pill deploy-stepper__status-pill--${status}`}>
            {status === 'idle' && 'Chưa bắt đầu'}
            {status === 'running' && `Đang chạy: Bước ${currentStep}/5`}
            {status === 'success' && 'Hoàn thành 100%'}
            {status === 'error' && 'Gặp sự cố tại bước ' + currentStep}
          </span>
        </div>

        <div className="deploy-stepper__grid">
          {DEPLOY_STEPS.map((step) => {
            const Icon = step.icon;
            const stepStatus = stepStatuses[step.id];
            const isCurrent = currentStep === step.id && status === 'running';

            return (
              <div
                key={step.id}
                className={`deploy-step-item deploy-step-item--${stepStatus} ${isCurrent ? 'active' : ''}`}
              >
                <div className="deploy-step-item__icon-wrap">
                  {stepStatus === 'running' && <Loader2 size={16} className="spin text-accent" />}
                  {stepStatus === 'success' && <CheckCircle2 size={16} className="text-success" />}
                  {stepStatus === 'error' && <AlertTriangle size={16} className="text-danger" />}
                  {stepStatus === 'pending' && <Icon size={16} className="text-muted" />}
                </div>

                <div className="deploy-step-item__content">
                  <div className="deploy-step-item__title">
                    <span className="step-num">B{step.id}.</span> {step.name}
                  </div>
                  <div className="deploy-step-item__desc">{step.desc}</div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* ── Status Banner ── */}
      {status !== 'idle' && (
        <div className={`deploy-viewer__badge deploy-viewer__badge--${status}`}>
          {status === 'running' && <Loader2 size={16} className="spin" />}
          {status === 'success' && <CheckCircle2 size={16} />}
          {status === 'error' && <AlertTriangle size={16} />}
          <span>
            {status === 'running' && `Đang thực thi Bước ${currentStep}/5: ${DEPLOY_STEPS[currentStep - 1]?.name || 'Triển khai'}...`}
            {status === 'success' && '🎉 Triển khai 1-Click SSH Deploy thành công hoàn toàn! Container đã live.'}
            {status === 'error' && '❌ Quy trình triển khai dừng lại do có lỗi. Vui lòng kiểm tra chi tiết console bên dưới.'}
          </span>
        </div>
      )}

      {/* ── Success Interactive Endpoint Result Box ── */}
      {status === 'success' && (
        <div className="deploy-endpoints-card">
          <div className="deploy-endpoints__header">
            <div className="deploy-endpoints__title">
              <CheckCircle2 size={18} className="text-success" />
              <span>Đường Dẫn Truy Cập Ứng Dụng (Application Endpoints)</span>
            </div>
            <span className="badge badge--success">Ready / Live 🟢</span>
          </div>

          <div className="deploy-endpoints__list">
            {/* Primary Direct Endpoint */}
            <div className="deploy-endpoint-row deploy-endpoint-row--main">
              <div className="deploy-endpoint-info">
                <div className="deploy-endpoint-label">
                  <Server size={14} />
                  <span>Cổng trực tiếp VPS (Primary Host Port)</span>
                </div>
                <code className="deploy-endpoint-url">{directAppUrl}</code>
              </div>
              <div className="deploy-endpoint-actions">
                <button
                  type="button"
                  className="deploy-copy-btn"
                  onClick={() => handleCopyLink(directAppUrl, 'direct')}
                  title="Sao chép URL"
                >
                  {copiedUrl === 'direct' ? <Check size={13} className="text-success" /> : <Copy size={13} />}
                  <span>{copiedUrl === 'direct' ? 'Đã chép' : 'Sao chép'}</span>
                </button>
                <a
                  href={directAppUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="deploy-open-btn"
                >
                  <span>Mở ứng dụng ngay</span>
                  <ExternalLink size={13} />
                </a>
              </div>
            </div>

            {/* Nginx Domain Endpoint if enabled */}
            {nginxDomainUrl && (
              <div className="deploy-endpoint-row">
                <div className="deploy-endpoint-info">
                  <div className="deploy-endpoint-label">
                    <Globe size={14} />
                    <span>Tên miền Nginx Domain</span>
                  </div>
                  <code className="deploy-endpoint-url">{nginxDomainUrl}</code>
                </div>
                <div className="deploy-endpoint-actions">
                  <button
                    type="button"
                    className="deploy-copy-btn"
                    onClick={() => handleCopyLink(nginxDomainUrl, 'nginx')}
                  >
                    {copiedUrl === 'nginx' ? <Check size={13} className="text-success" /> : <Copy size={13} />}
                    <span>{copiedUrl === 'nginx' ? 'Đã chép' : 'Sao chép'}</span>
                  </button>
                  <a
                    href={nginxDomainUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="deploy-open-btn"
                  >
                    <span>Mở Domain</span>
                    <ExternalLink size={13} />
                  </a>
                </div>
              </div>
            )}

            {/* sslip.io Auto-Domain if enabled */}
            {sslAutoDomainUrl && (
              <div className="deploy-endpoint-row">
                <div className="deploy-endpoint-info">
                  <div className="deploy-endpoint-label">
                    <ShieldCheck size={14} />
                    <span>Auto HTTPS SSL (sslip.io)</span>
                  </div>
                  <code className="deploy-endpoint-url">{sslAutoDomainUrl}</code>
                </div>
                <div className="deploy-endpoint-actions">
                  <button
                    type="button"
                    className="deploy-copy-btn"
                    onClick={() => handleCopyLink(sslAutoDomainUrl, 'sslip')}
                  >
                    {copiedUrl === 'sslip' ? <Check size={13} className="text-success" /> : <Copy size={13} />}
                    <span>{copiedUrl === 'sslip' ? 'Đã chép' : 'Sao chép'}</span>
                  </button>
                  <a
                    href={sslAutoDomainUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="deploy-open-btn"
                  >
                    <span>Mở HTTPS</span>
                    <ExternalLink size={13} />
                  </a>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── macOS Style Terminal Console ── */}
      <div className="deploy-viewer__terminal-window">
        <div className="deploy-viewer__mac-header">
          <div className="deploy-viewer__mac-dots">
            <span className="dot dot--red" />
            <span className="dot dot--yellow" />
            <span className="dot dot--green" />
          </div>
          <div className="deploy-viewer__mac-title">
            <TerminalIcon size={13} />
            <span>Terminal Live Deploy Stream (WebSocket Port 8088)</span>
          </div>
          <div className="deploy-viewer__mac-actions">
            <button
              type="button"
              className="deploy-viewer__action-btn"
              onClick={handleClearTerminal}
              title="Xoá màn hình terminal"
            >
              <Trash2 size={12} />
            </button>
            <button
              type="button"
              className="deploy-viewer__action-btn"
              onClick={() => setIsExpanded(!isExpanded)}
              title={isExpanded ? 'Thu nhỏ' : 'Mở rộng'}
            >
              {isExpanded ? <Minimize2 size={12} /> : <Maximize2 size={12} />}
            </button>
          </div>
        </div>

        <div className="deploy-viewer__terminal-wrap">
          <div ref={terminalRef} className="deploy-viewer__terminal" />
        </div>
      </div>
    </div>
  );
}

export default DeployLogViewer;
