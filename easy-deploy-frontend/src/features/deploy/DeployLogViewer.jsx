import { useEffect, useRef, useState } from 'react';
import { Terminal } from '@xterm/xterm';
import { FitAddon } from '@xterm/addon-fit';
import { Rocket, Loader2, CheckCircle2, AlertTriangle } from 'lucide-react';
import '@xterm/xterm/css/xterm.css';
import './DeployLogViewer.css';

function DeployLogViewer({ config, credentials, onFinished }) {
  const terminalRef = useRef(null);
  const termInstanceRef = useRef(null);
  const fitAddonRef = useRef(null);
  const wsRef = useRef(null);

  const [deploying, setDeploying] = useState(false);
  const [status, setStatus] = useState('idle'); // 'idle' | 'running' | 'success' | 'error'

  const start1ClickDeploy = () => {
    if (deploying) return;

    setDeploying(true);
    setStatus('running');

    // 1. Khởi tạo Terminal
    if (!termInstanceRef.current && terminalRef.current) {
      const term = new Terminal({
        cursorBlink: true,
        fontSize: 13,
        fontFamily: "'JetBrains Mono', monospace",
        theme: {
          background: '#0d1117',
          foreground: '#e6edf3',
          cursor: '#58a6ff',
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
      // Gửi Payload chứa Config và Thông tin VPS
      ws.send(
        JSON.stringify({
          config,
          credentials,
        })
      );
    };

    ws.onmessage = (event) => {
      term?.write(event.data);

      if (event.data.includes('[SUCCESS]')) {
        setStatus('success');
        setDeploying(false);
        onFinished?.('success');
      } else if (event.data.includes('[ERROR]') || event.data.includes('[FATAL ERROR]')) {
        setStatus('error');
        setDeploying(false);
        onFinished?.('error');
      }
    };

    ws.onerror = () => {
      term?.write('\r\n\u001b[31m[WebSocket Error] Lỗi kết nối server!\u001b[0m\r\n');
      setDeploying(false);
      setStatus('error');
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

  useEffect(() => {
    return () => {
      if (wsRef.current) wsRef.current.close();
      if (termInstanceRef.current) termInstanceRef.current.dispose();
    };
  }, []);

  return (
    <div className="deploy-viewer">
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
              <Loader2 size={18} className="spin" />
              <span>Đang tự động triển khai tới VPS ({credentials.host})...</span>
            </>
          ) : (
            <>
              <Rocket size={18} />
              <span>🚀 Bắt đầu Khởi chạy 1-Click SSH Deploy</span>
            </>
          )}
        </button>

        {!credentials?.host && (
          <span className="deploy-viewer__hint">
            ⚠️ Vui lòng điền IP VPS ở trên trước khi bấm khởi chạy.
          </span>
        )}
      </div>

      {/* ── Status Banner ── */}
      {status !== 'idle' && (
        <div className={`deploy-viewer__badge deploy-viewer__badge--${status}`}>
          {status === 'running' && <Loader2 size={14} className="spin" />}
          {status === 'success' && <CheckCircle2 size={14} />}
          {status === 'error' && <AlertTriangle size={14} />}
          <span>
            {status === 'running' && 'Đang tải file cấu hình lên VPS & thực thi Docker Compose...'}
            {status === 'success' && 'Triển khai 1-Click Deploy thành công! Ứng dụng đã sẵn sàng.'}
            {status === 'error' && 'Quy trình triển khai gặp lỗi. Vui lòng xem thông tin chi tiết dưới log console.'}
          </span>
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
            <span>Terminal Live Deploy Stream (WebSocket Port 8088)</span>
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

