import { useEffect, useRef, useState } from 'react';
import { Terminal } from '@xterm/xterm';
import { FitAddon } from '@xterm/addon-fit';
import {
  Terminal as TermIcon,
  Play,
  Square,
  Loader2,
  BookmarkPlus,
  Trash2,
  Code,
  Plus,
  Copy,
  Check,
  Send,
  Server,
  Sparkles,
} from 'lucide-react';
import { useVps } from '../../context/VpsContext';
import '@xterm/xterm/css/xterm.css';
import './WebTerminal.css';

function WebTerminal() {
  const {
    vpsList,
    saveVpsProfile,
    deleteVpsProfile,
    snippets,
    addSnippet,
    deleteSnippet,
  } = useVps();

  const terminalRef = useRef(null);
  const termInstanceRef = useRef(null);
  const wsRef = useRef(null);
  const fitAddonRef = useRef(null);

  const [form, setForm] = useState({
    host: vpsList[0]?.host || '',
    port: vpsList[0]?.port || 22,
    username: vpsList[0]?.username || '',
    password: vpsList[0]?.password || '',
  });

  const [selectedVpsId, setSelectedVpsId] = useState(vpsList[0]?.id || '');
  const [connected, setConnected] = useState(false);
  const [connecting, setConnecting] = useState(false);
  const [copiedSnippetId, setCopiedSnippetId] = useState(null);

  // Modal thêm snippet mới
  const [showAddSnippetModal, setShowAddSnippetModal] = useState(false);
  const [newSnippetTitle, setNewSnippetTitle] = useState('');
  const [newSnippetCommand, setNewSnippetCommand] = useState('');
  const [newSnippetCategory, setNewSnippetCategory] = useState('Docker');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSelectVps = (e) => {
    const id = e.target.value;
    setSelectedVpsId(id);
    const selected = vpsList.find((v) => v.id === id);
    if (selected) {
      setForm({
        host: selected.host || '',
        port: selected.port || 22,
        username: selected.username || '',
        password: selected.password || '',
      });
    }
  };

  const handleSaveCurrentVps = () => {
    const defaultName = selectedVpsId
      ? vpsList.find((v) => v.id === selectedVpsId)?.name
      : `${form.username}@${form.host}`;
    const name = prompt('Nhập tên gợi nhớ cho máy chủ VPS này:', defaultName || 'VPS mới');
    if (name) {
      saveVpsProfile({
        id: selectedVpsId || undefined,
        name,
        host: form.host,
        port: form.port,
        username: form.username,
        password: form.password,
      });
      alert(`Đã lưu máy chủ "${name}" vào danh sách!`);
    }
  };

  const handleDeleteCurrentVps = () => {
    if (!selectedVpsId) return;
    const vps = vpsList.find((v) => v.id === selectedVpsId);
    if (confirm(`Bạn có chắc muốn xoá máy chủ "${vps?.name || selectedVpsId}" khỏi danh sách quản lý?`)) {
      deleteVpsProfile(selectedVpsId);
      setSelectedVpsId('');
    }
  };

  const handleConnect = () => {
    if (!form.host || !form.username) return;

    setConnecting(true);

    // 1. Khởi tạo Terminal & FitAddon
    if (!termInstanceRef.current && terminalRef.current) {
      const term = new Terminal({
        cursorBlink: true,
        fontSize: 14,
        fontFamily: "'JetBrains Mono', monospace",
        theme: {
          background: '#090d16',
          foreground: '#e2e8f0',
          cursor: '#38bdf8',
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

    // 2. Mở kết nối WebSocket tới Backend Spring Boot
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.hostname}:8088/ws/ssh`;
    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen = () => {
      setConnected(true);
      setConnecting(false);
      // Gửi thông tin SSH auth ở payload đầu tiên
      ws.send(JSON.stringify(form));
    };

    ws.onmessage = (event) => {
      term?.write(event.data);
    };

    ws.onerror = () => {
      term?.write('\r\n\u001b[31m[WebSocket Error] Kết nối bị lỗi!\u001b[0m\r\n');
      setConnecting(false);
      setConnected(false);
    };

    ws.onclose = () => {
      term?.write('\r\n\u001b[33m[SSH] Kết nối đã đóng.\u001b[0m\r\n');
      setConnected(false);
      setConnecting(false);
    };

    // 3. Lắng nghe phím bấm từ xterm và đẩy sang WebSocket
    const onDataDisposable = term.onData((data) => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.send(data);
      }
    });

    // 4. Lắng nghe resize window để fit vừa container
    const handleResize = () => fitAddonRef.current?.fit();
    window.addEventListener('resize', handleResize);

    return () => {
      onDataDisposable.dispose();
      window.removeEventListener('resize', handleResize);
    };
  };

  const handleDisconnect = () => {
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
    setConnected(false);
    setConnecting(false);
  };

  const handleRunSnippet = (snip) => {
    if (connected && wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(snip.command + '\r');
      termInstanceRef.current?.focus();
    } else {
      navigator.clipboard.writeText(snip.command);
      setCopiedSnippetId(snip.id);
      setTimeout(() => setCopiedSnippetId(null), 2000);
    }
  };

  const handleSaveNewSnippet = (e) => {
    e.preventDefault();
    if (!newSnippetTitle.trim() || !newSnippetCommand.trim()) return;
    addSnippet({
      title: newSnippetTitle.trim(),
      command: newSnippetCommand.trim(),
      category: newSnippetCategory || 'General',
    });
    setNewSnippetTitle('');
    setNewSnippetCommand('');
    setShowAddSnippetModal(false);
  };

  // Cleanup khi unmount
  useEffect(() => {
    return () => {
      if (wsRef.current) wsRef.current.close();
      if (termInstanceRef.current) termInstanceRef.current.dispose();
    };
  }, []);

  return (
    <div className="web-terminal">
      {/* ── Quản lý Nhiều VPS & Form SSH Card ── */}
      <div className="wt-form-card">
        <div className="wt-form__header">
          <div className="wt-form__title-group">
            <div className="wt-form__icon-wrap">
              <TermIcon size={16} />
            </div>
            <div>
              <h3 className="wt-form__title">Web SSH Terminal — Quản trị VPS</h3>
              <p className="wt-form__subtitle">Kết nối shell tương tác trực tiếp trên trình duyệt tới bất kỳ máy chủ VPS nào.</p>
            </div>
          </div>

          <div className="wt-vps-picker">
            <Server size={14} className="wt-vps-icon" />
            <select
              className="wt-vps-select"
              value={selectedVpsId}
              onChange={handleSelectVps}
              disabled={connected || connecting}
            >
              <option value="">⚡ Chọn máy chủ đã lưu ({vpsList.length} VPS)</option>
              {vpsList.map((vps) => (
                <option key={vps.id} value={vps.id}>
                  🖥️ {vps.name} ({vps.username}@{vps.host}:{vps.port})
                </option>
              ))}
            </select>

            <button
              type="button"
              className="wt-vps-action-btn"
              title="Lưu cấu hình VPS hiện tại"
              onClick={handleSaveCurrentVps}
              disabled={connected || connecting}
            >
              <BookmarkPlus size={13} /> Lưu VPS
            </button>

            {selectedVpsId && (
              <button
                type="button"
                className="wt-vps-action-btn wt-vps-action-btn--danger"
                title="Xoá VPS đang chọn khỏi danh sách"
                onClick={handleDeleteCurrentVps}
                disabled={connected || connecting}
              >
                <Trash2 size={13} />
              </button>
            )}
          </div>
        </div>

        <div className="wt-form__grid">
          <div className="wt-input-wrap">
            <input
              type="text"
              name="host"
              placeholder="Host / IP (e.g. 103.179.x.x)"
              value={form.host}
              onChange={handleChange}
              disabled={connected || connecting}
            />
          </div>
          <div className="wt-input-wrap wt-input-wrap--sm">
            <input
              type="number"
              name="port"
              placeholder="Port (22)"
              value={form.port}
              onChange={handleChange}
              disabled={connected || connecting}
            />
          </div>
          <div className="wt-input-wrap">
            <input
              type="text"
              name="username"
              placeholder="Username (root)"
              value={form.username}
              onChange={handleChange}
              disabled={connected || connecting}
            />
          </div>
          <div className="wt-input-wrap">
            <input
              type="password"
              name="password"
              placeholder="Mật khẩu SSH"
              value={form.password}
              onChange={handleChange}
              disabled={connected || connecting}
            />
          </div>

          {!connected ? (
            <button
              type="button"
              className="wt-btn wt-btn--connect"
              onClick={handleConnect}
              disabled={connecting || !form.host || !form.username}
            >
              {connecting ? <Loader2 size={15} className="spin" /> : <Play size={15} />}
              <span>{connecting ? 'Đang kết nối...' : 'Mở Terminal'}</span>
            </button>
          ) : (
            <button
              type="button"
              className="wt-btn wt-btn--disconnect"
              onClick={handleDisconnect}
            >
              <Square size={15} />
              <span>Ngắt kết nối</span>
            </button>
          )}
        </div>
      </div>

      {/* ── Snippets Toolbar ── */}
      <div className="wt-snippets-card">
        <div className="wt-snippets__header">
          <div className="wt-snippets__title">
            <Code size={15} className="text-accent" />
            <span>Thư viện Snippet Câu lệnh Thường dùng ({snippets.length})</span>
          </div>

          <button
            type="button"
            className="wt-add-snippet-btn"
            onClick={() => setShowAddSnippetModal(true)}
          >
            <Plus size={13} /> Thêm Snippet
          </button>
        </div>

        <div className="wt-snippets__grid">
          {snippets.map((snip) => (
            <div
              key={snip.id}
              className="wt-snippet-chip"
              onClick={() => handleRunSnippet(snip)}
              title={connected ? `Nhấn để chạy: ${snip.command}` : `Nhấn để sao chép: ${snip.command}`}
            >
              <span className="wt-snippet-cat">{snip.category}</span>
              <span className="wt-snippet-name">{snip.title}</span>
              <code className="wt-snippet-cmd">{snip.command}</code>

              <div className="wt-snippet-actions">
                {connected ? (
                  <Send size={12} className="wt-snippet-icon wt-snippet-icon--send" />
                ) : (
                  copiedSnippetId === snip.id ? (
                    <Check size={12} className="wt-snippet-icon text-success" />
                  ) : (
                    <Copy size={12} className="wt-snippet-icon" />
                  )
                )}
                <button
                  type="button"
                  className="wt-snippet-del"
                  title="Xoá snippet"
                  onClick={(e) => {
                    e.stopPropagation();
                    deleteSnippet(snip.id);
                  }}
                >
                  <Trash2 size={11} />
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* ── Modal Thêm Snippet mới ── */}
      {showAddSnippetModal && (
        <div className="wt-modal-overlay">
          <div className="wt-modal">
            <div className="wt-modal__header">
              <Code size={16} />
              <span>Thêm Snippet Câu lệnh Mới</span>
            </div>
            <form onSubmit={handleSaveNewSnippet} className="wt-modal__form">
              <div className="wt-modal__field">
                <label>Tên / Tiêu đề gợi nhớ</label>
                <input
                  type="text"
                  placeholder="e.g. Kiểm tra RAM & Dung lượng Disk"
                  value={newSnippetTitle}
                  onChange={(e) => setNewSnippetTitle(e.target.value)}
                  required
                />
              </div>

              <div className="wt-modal__field">
                <label>Phân loại (Category)</label>
                <input
                  type="text"
                  placeholder="e.g. Docker, Nginx, System, Security"
                  value={newSnippetCategory}
                  onChange={(e) => setNewSnippetCategory(e.target.value)}
                />
              </div>

              <div className="wt-modal__field">
                <label>Câu lệnh Bash / Shell</label>
                <textarea
                  placeholder="e.g. docker compose ps && free -h"
                  value={newSnippetCommand}
                  onChange={(e) => setNewSnippetCommand(e.target.value)}
                  rows={3}
                  required
                />
              </div>

              <div className="wt-modal__actions">
                <button
                  type="button"
                  className="wt-modal-btn wt-modal-btn--cancel"
                  onClick={() => setShowAddSnippetModal(false)}
                >
                  Hủy
                </button>
                <button type="submit" className="wt-modal-btn wt-modal-btn--submit">
                  Lưu Snippet
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── macOS Style XTerm Container ── */}
      <div className="wt-terminal-window">
        <div className="wt-mac-header">
          <div className="wt-mac-dots">
            <span className="dot dot--red" />
            <span className="dot dot--yellow" />
            <span className="dot dot--green" />
          </div>
          <div className="wt-mac-title">
            <span>Web SSH Terminal — {connected ? `${form.username}@${form.host}` : 'Chưa kết nối'}</span>
          </div>
        </div>
        <div className="wt-container">
          <div ref={terminalRef} className="wt-xterm" />
        </div>
      </div>
    </div>
  );
}

export default WebTerminal;
