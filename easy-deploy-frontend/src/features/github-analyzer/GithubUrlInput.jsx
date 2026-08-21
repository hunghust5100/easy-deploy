import { useState } from 'react';
import { Search, Loader2, CheckCircle2, AlertCircle, ArrowRight, Settings, Rocket, FileCode2 } from 'lucide-react';
import { analyzeRepo } from '../../services/githubService';
import { useConfig } from '../../context/ConfigContext';
import './GithubUrlInput.css';

const QUICK_PRESETS = [
  { label: 'EasyDeploy (Java)', url: 'https://github.com/hunghust5100/easy-deploy' },
  { label: 'Express (Node.js)', url: 'https://github.com/expressjs/express' },
];

function GithubUrlInput() {
  const { applyGithubResult, setGeneratorStep } = useConfig();
  const [url, setUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState(null);
  const [message, setMessage] = useState('');
  const [detectedStack, setDetectedStack] = useState(null);

  const runAnalysis = async (targetUrl) => {
    const finalUrl = (targetUrl || url).trim();
    if (!finalUrl) return;

    setLoading(true);
    setStatus(null);
    setMessage('');
    setDetectedStack(null);

    try {
      const data = await analyzeRepo(finalUrl);
      if (data.suggestedConfig) {
        await applyGithubResult(data.suggestedConfig);
        setStatus('success');
        const svcCount = data.suggestedConfig?.services?.length || 0;
        const svcMsg = svcCount > 1 ? ` — Tìm thấy ${svcCount} modules/packages trong dự án.` : '';
        setMessage(`Phân tích thành công ${data.scannedFilesCount} tệp tin${svcMsg}`);
        if (data.suggestedConfig?.techStack) {
          setDetectedStack(data.suggestedConfig.techStack);
        }
      }
    } catch (err) {
      setStatus('error');
      setMessage(err.friendlyMessage || err.response?.data?.error || err.message || 'Không thể phân tích repository. Vui lòng kiểm tra lại URL.');
    } finally {
      setLoading(false);
    }
  };

  const handleAnalyze = () => runAnalysis(url);

  const handleSelectPreset = (presetUrl) => {
    setUrl(presetUrl);
    runAnalysis(presetUrl);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') handleAnalyze();
  };

  const scrollToSection = (sectionId) => {
    const el = document.getElementById(sectionId);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  return (
    <div className="github-hero">
      <div className="github-hero__header">
        <h2 className="github-hero__title">Tự Động Nhận Diện Từ GitHub</h2>
      </div>

      <div className="github-hero__search-card">
        <div className="github-input__field-wrap">
          <svg className="github-input__icon" viewBox="0 0 16 16" width="18" height="18" fill="currentColor" aria-hidden="true">
            <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z" />
          </svg>
          <input
            id="github-url"
            type="url"
            className="github-input__field"
            placeholder="https://github.com/username/repository"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={loading}
          />
        </div>
        <button
          type="button"
          className="github-input__btn"
          onClick={handleAnalyze}
          disabled={loading || !url.trim()}
        >
          {loading ? <Loader2 size={16} className="spin" /> : <Search size={16} />}
          <span>{loading ? 'Đang quét...' : 'Phân tích'}</span>
        </button>
      </div>

      {/* Quick Presets */}
      <div className="github-presets">
        <span className="github-presets__label">Mẫu thử:</span>
        {QUICK_PRESETS.map((p) => (
          <button
            key={p.url}
            type="button"
            className="github-preset-btn"
            onClick={() => handleSelectPreset(p.url)}
            disabled={loading}
          >
            <span>{p.label}</span>
            <ArrowRight size={11} />
          </button>
        ))}
      </div>

      {/* Status Feedback & Action Jump Bar */}
      {status && (
        <div className={`github-input__status-container github-input__status--${status}`}>
          <div className="github-input__status-top">
            <div className="github-status-left">
              {status === 'success' ? (
                <CheckCircle2 size={18} className="status-icon" />
              ) : (
                <AlertCircle size={18} className="status-icon" />
              )}
              <span>{message}</span>
            </div>

            {detectedStack && (
              <div className="github-detected-stack">
                <span>Công nghệ:</span>
                <code>{detectedStack}</code>
              </div>
            )}
          </div>

          {/* Quick Action Navigation after scan */}
          {status === 'success' && (
            <div className="github-quick-actions">
              <button
                type="button"
                className="github-action-btn github-action-btn--config"
                onClick={() => scrollToSection('config-section')}
              >
                <Settings size={14} />
                <span>Chỉnh Sửa Cấu Hình Bên Dưới</span>
                <ArrowRight size={12} />
              </button>

              <button
                type="button"
                className="github-action-btn github-action-btn--deploy"
                onClick={() => {
                  setGeneratorStep(2);
                  window.scrollTo({ top: 0, behavior: 'smooth' });
                }}
              >
                <Rocket size={14} />
                <span>Tiếp Tục: Xem File & Deploy</span>
                <ArrowRight size={12} />
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default GithubUrlInput;
