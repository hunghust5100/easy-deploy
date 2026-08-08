import { useState } from 'react';
import { Search, Loader2, CheckCircle2, AlertCircle } from 'lucide-react';
import { analyzeRepo } from '../../services/githubService';
import { useConfig } from '../../context/ConfigContext';
import './GithubUrlInput.css';

function GithubUrlInput() {
  const { applyGithubResult } = useConfig();
  const [url, setUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState(null); // 'success' | 'error'
  const [message, setMessage] = useState('');

  const handleAnalyze = async () => {
    if (!url.trim()) return;
    setLoading(true);
    setStatus(null);
    setMessage('');

    try {
      const data = await analyzeRepo(url.trim());
      applyGithubResult(data.suggestedConfig);
      setStatus('success');
      setMessage(`Đã quét ${data.scannedFilesCount} files thành công!`);
    } catch (err) {
      setStatus('error');
      setMessage(err.response?.data?.error || 'Không thể phân tích repository');
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') handleAnalyze();
  };

  return (
    <div className="github-input">
      <label className="github-input__label" htmlFor="github-url">
        GitHub Repository URL
      </label>
      <div className="github-input__row">
        <div className="github-input__field-wrap">
          <Search size={15} className="github-input__icon" />
          <input
            id="github-url"
            type="url"
            className="github-input__field"
            placeholder="https://github.com/user/repo"
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
          {loading ? <Loader2 size={15} className="spin" /> : <Search size={15} />}
          <span>{loading ? 'Đang quét...' : 'Phân tích'}</span>
        </button>
      </div>

      {status && (
        <div className={`github-input__status github-input__status--${status}`}>
          {status === 'success' ? <CheckCircle2 size={14} /> : <AlertCircle size={14} />}
          <span>{message}</span>
        </div>
      )}
    </div>
  );
}

export default GithubUrlInput;
