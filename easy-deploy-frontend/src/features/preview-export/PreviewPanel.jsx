import { useState, useCallback } from 'react';
import {
  RefreshCw,
  Download,
  Loader2,
  FileCode2,
  Copy,
  Check,
  RotateCcw,
  Edit3,
  Container,
  Layers,
  Key,
  Globe,
  GitBranch,
  FileText,
  Terminal,
  FileArchive,
} from 'lucide-react';
import { useConfig } from '../../context/ConfigContext';
import { generateCustomZip } from '../../services/downloadService';
import './PreviewPanel.css';

function getFileIcon(filename) {
  if (filename === 'Dockerfile' || filename.includes('Docker')) return <Container size={14} className="file-icon-docker" />;
  if (filename.includes('docker-compose')) return <Layers size={14} className="file-icon-compose" />;
  if (filename.startsWith('.env')) return <Key size={14} className="file-icon-env" />;
  if (filename.includes('nginx')) return <Globe size={14} className="file-icon-nginx" />;
  if (filename.includes('deploy.yml') || filename.includes('github')) return <GitBranch size={14} className="file-icon-cicd" />;
  if (filename.endsWith('.sh')) return <Terminal size={14} className="file-icon-sh" />;
  return <FileText size={14} />;
}

function PreviewPanel() {
  const {
    config,
    previewFiles,
    originalPreviewFiles,
    updatePreviewFile,
    resetPreviewFile,
    activeFileId,
    setActiveFileId,
    previewLoading,
    fetchPreview,
  } = useConfig();

  const [downloading, setDownloading] = useState(false);
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState(null);

  const handleRefreshPreview = async () => {
    setError(null);
    try {
      const files = await fetchPreview();
      if (files && !files[activeFileId]) {
        const firstKey = Object.keys(files)[0];
        if (firstKey) setActiveFileId(firstKey);
      }
    } catch (err) {
      setError(err?.response?.data?.error || 'Không thể sinh mã xem trước');
    }
  };

  const handleDownload = async () => {
    if (!previewFiles) return;
    setDownloading(true);
    setError(null);
    try {
      await generateCustomZip(previewFiles, config.appName || 'app');
    } catch (err) {
      console.error(err);
      setError('Không thể đóng gói file ZIP');
    } finally {
      setDownloading(false);
    }
  };

  const handleCopyCode = useCallback(() => {
    if (!previewFiles || !activeFileId) return;
    const content = previewFiles[activeFileId] || '';
    navigator.clipboard.writeText(content);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }, [previewFiles, activeFileId]);

  const handleDownloadSingleFile = () => {
    if (!previewFiles || !activeFileId) return;
    const content = previewFiles[activeFileId] || '';
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = activeFileId;
    a.click();
    URL.revokeObjectURL(url);
  };

  const fileEntries = previewFiles ? Object.entries(previewFiles) : [];
  const currentContent = previewFiles ? (previewFiles[activeFileId] || '') : '';
  const originalContent = originalPreviewFiles ? (originalPreviewFiles[activeFileId] || '') : '';
  const isFileModified = previewFiles && originalPreviewFiles && currentContent !== originalContent;

  return (
    <div className="preview-panel" id="preview-section">
      {/* ── Action Bar Header ── */}
      <div className="preview-panel__actions-card">
        <div className="preview-panel__title-group">
          <FileCode2 size={18} className="preview-panel__header-icon" />
          <h3 className="preview-panel__header-title">
            DevOps Config Files ({fileEntries.length} Tệp tin)
          </h3>
        </div>

        <div className="preview-panel__btn-group">
          <button
            type="button"
            className="preview-panel__btn preview-panel__btn--secondary"
            onClick={handleRefreshPreview}
            disabled={previewLoading}
            title="Sinh lại code theo thông số form"
          >
            {previewLoading ? <Loader2 size={13} className="spin" /> : <RefreshCw size={13} />}
            <span>{previewLoading ? 'Đang tạo...' : 'Làm mới'}</span>
          </button>

          {isFileModified && (
            <button
              type="button"
              className="preview-panel__btn preview-panel__btn--secondary"
              onClick={() => resetPreviewFile(activeFileId)}
              title="Khôi phục file về ban đầu"
            >
              <RotateCcw size={13} />
              <span>Khôi phục</span>
            </button>
          )}

          <button
            type="button"
            className="preview-panel__btn preview-panel__btn--secondary"
            onClick={handleCopyCode}
            disabled={!previewFiles}
            title="Sao chép nội dung file"
          >
            {copied ? <Check size={13} className="text-success" /> : <Copy size={13} />}
            <span>{copied ? 'Đã chép!' : 'Sao chép'}</span>
          </button>

          <button
            type="button"
            className="preview-panel__btn preview-panel__btn--secondary"
            onClick={handleDownloadSingleFile}
            disabled={!previewFiles}
            title="Tải tệp tin này"
          >
            <Download size={13} />
            <span>Tải tệp</span>
          </button>

          <button
            type="button"
            className="preview-panel__btn preview-panel__btn--accent"
            onClick={handleDownload}
            disabled={downloading || !previewFiles}
            title="Tải về file .ZIP"
          >
            {downloading ? <Loader2 size={14} className="spin" /> : <FileArchive size={14} />}
            <span>{downloading ? 'Đang nén...' : 'Tải ZIP'}</span>
          </button>
        </div>
      </div>

      {error && <div className="preview-panel__error">{error}</div>}

      {/* ── File Tabs + Interactive Code Editor ── */}
      {previewLoading ? (
        <div className="preview-panel__empty">
          <Loader2 size={28} className="spin preview-panel__empty-icon" />
          <p>Đang sinh mã cấu hình...</p>
        </div>
      ) : fileEntries.length > 0 ? (
        <div className="preview-panel__viewer">
          {/* File Selector Tabs */}
          <div className="preview-panel__tabs" role="tablist">
            {fileEntries.map(([filename]) => {
              const modified = originalPreviewFiles && previewFiles[filename] !== originalPreviewFiles[filename];
              return (
                <button
                  key={filename}
                  type="button"
                  role="tab"
                  aria-selected={activeFileId === filename}
                  className={`preview-panel__tab ${activeFileId === filename ? 'preview-panel__tab--active' : ''}`}
                  onClick={() => setActiveFileId(filename)}
                >
                  {getFileIcon(filename)}
                  <span>{filename}</span>
                  {modified && <span className="preview-panel__dot-modified" title="Đã chỉnh sửa" />}
                </button>
              );
            })}
          </div>

          {/* Editor Header Info */}
          <div className="preview-panel__editor-toolbar">
            <div className="preview-panel__file-info">
              <span className="preview-panel__filename">{activeFileId}</span>
              {isFileModified ? (
                <span className="preview-panel__status preview-panel__status--modified">
                  <Edit3 size={11} /> Đã chỉnh sửa
                </span>
              ) : (
                <span className="preview-panel__status preview-panel__status--clean">
                  Mặc định
                </span>
              )}
            </div>
            <span className="preview-panel__edit-hint">💡 Có thể chỉnh sửa trực tiếp bên dưới trước khi tải về hoặc deploy</span>
          </div>

          {/* Interactive Code Editor TextArea */}
          <div className="preview-panel__editor-wrapper">
            <textarea
              className="preview-panel__editor"
              value={currentContent}
              onChange={(e) => updatePreviewFile(activeFileId, e.target.value)}
              placeholder="Nội dung file cấu hình..."
              spellCheck={false}
            />
          </div>
        </div>
      ) : (
        <div className="preview-panel__empty">
          <RefreshCw size={28} className="preview-panel__empty-icon" />
          <p>Chưa có dữ liệu cấu hình. Nhấn nút bên dưới để tạo ngay.</p>
          <button
            type="button"
            className="preview-panel__btn preview-panel__btn--accent"
            onClick={handleRefreshPreview}
          >
            Làm mới Code Xem trước
          </button>
        </div>
      )}
    </div>
  );
}

export default PreviewPanel;
