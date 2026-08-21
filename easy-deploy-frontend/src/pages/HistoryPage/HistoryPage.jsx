import { useState, useEffect, useCallback } from 'react';
import { History, RefreshCw, CheckCircle, XCircle, Clock, Server, FileCode, Terminal, X } from 'lucide-react';
import * as deploymentService from '../../services/deploymentService';
import { useAuth } from '../../context/AuthContext';
import './HistoryPage.css';

function HistoryPage() {
  const { currentUser } = useAuth();
  const [deployments, setDeployments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedLog, setSelectedLog] = useState(null);

  const fetchHistory = useCallback(async (userObj) => {
    const targetUser = userObj || currentUser;
    if (!targetUser?.id) {
      setDeployments([]);
      return;
    }
    setLoading(true);
    try {
      const data = await deploymentService.getDeployments({ userId: targetUser.id });
      setDeployments(data);
    } catch (err) {
      console.warn('Lỗi tải lịch sử deploy:', err);
    } finally {
      setLoading(false);
    }
  }, [currentUser]);

  useEffect(() => {
    fetchHistory(currentUser);
  }, [currentUser, fetchHistory]);

  const viewFullLog = async (id) => {
    try {
      const detail = await deploymentService.getDeploymentById(id);
      setSelectedLog(detail);
    } catch (err) {
      alert('Không thể tải log: ' + err.message);
    }
  };

  const formatDate = (isoString) => {
    if (!isoString) return '--';
    try {
      const date = new Date(isoString);
      return date.toLocaleString('vi-VN');
    } catch {
      return isoString;
    }
  };

  return (
    <div className="history-page">
      <div className="history-page__header">
        <div>
          <h2 className="history-page__title">
            <History className="text-primary" size={24} />
            Lịch Sử Triển Khai
          </h2>
        </div>

        <button
          type="button"
          className="btn-action btn-action--secondary"
          onClick={() => fetchHistory()}
          disabled={loading}
        >
          <RefreshCw size={14} className={loading ? 'spin' : ''} />
          Làm mới
        </button>
      </div>

      {deployments.length === 0 ? (
        <div className="empty-state">
          <History size={48} className="empty-state__icon" />
          <h3>Chưa có phiên triển khai nào</h3>
          <p style={{ marginTop: '0.5rem' }}>
            Các phiên triển khai ứng dụng lên VPS sẽ được lưu lại tại đây.
          </p>
        </div>
      ) : (
        <div className="history-table-container">
          <table className="history-table">
            <thead>
              <tr>
                <th>Ứng dụng (App)</th>
                <th>Máy chủ VPS</th>
                <th>Trạng thái</th>
                <th>Bắt đầu</th>
                <th>Thời lượng</th>
                <th>Nguồn</th>
                <th style={{ textAlign: 'right' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {deployments.map((d) => (
                <tr key={d.id}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600 }}>
                      <FileCode size={16} className="text-primary" />
                      {d.appName || 'Ứng dụng Web'}
                    </div>
                  </td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#94a3b8' }}>
                      <Server size={14} />
                      {d.serverName || 'Máy chủ VPS'}
                    </div>
                  </td>
                  <td>
                    {d.status === 'SUCCESS' ? (
                      <span className="status-badge status-badge--success">
                        <CheckCircle size={12} /> Thành Công
                      </span>
                    ) : d.status === 'RUNNING' ? (
                      <span className="status-badge" style={{ background: 'rgba(56, 189, 248, 0.15)', color: '#38bdf8' }}>
                        <RefreshCw size={12} className="spin" /> Đang chạy
                      </span>
                    ) : (
                      <span className="status-badge status-badge--danger">
                        <XCircle size={12} /> Thất Bại
                      </span>
                    )}
                  </td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#cbd5e1', fontSize: '0.8rem' }}>
                      <Clock size={12} />
                      {formatDate(d.startedAt)}
                    </div>
                  </td>
                  <td>
                    {d.durationSeconds ? `${d.durationSeconds}s` : '--'}
                  </td>
                  <td>
                    <span style={{ fontSize: '0.75rem', background: 'rgba(255,255,255,0.05)', padding: '2px 6px', borderRadius: '4px' }}>
                      {d.triggerSource || 'WEB_UI'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <button
                      type="button"
                      className="btn-action btn-action--secondary"
                      style={{ padding: '0.35rem 0.65rem' }}
                      onClick={() => viewFullLog(d.id)}
                    >
                      <Terminal size={14} />
                      Xem Logs
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal Xem Full Log */}
      {selectedLog && (
        <div className="modal-overlay" onClick={() => setSelectedLog(null)}>
          <div className="modal-content" style={{ maxWidth: '800px' }} onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#fff' }}>
                  Nhật Ký Triển Khai: {selectedLog.appName || 'App'}
                </h3>
                <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>
                  Khởi chạy lúc: {formatDate(selectedLog.startedAt)} | Trạng thái: {selectedLog.status}
                </span>
              </div>
              <button type="button" className="btn-action btn-action--danger" onClick={() => setSelectedLog(null)}>
                <X size={16} />
              </button>
            </div>

            <div className="modal-body" style={{ padding: '1rem' }}>
              <div className="log-viewer-terminal">
                {/* eslint-disable-next-line no-control-regex */}
                {selectedLog.logContent ? selectedLog.logContent.replace(/\u001b\[[0-9;]*m/g, '') : 'Không có dữ liệu log ghi lại.'}
              </div>
            </div>

            <div className="modal-footer">
              <button type="button" className="btn-action btn-action--primary" onClick={() => setSelectedLog(null)}>
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default HistoryPage;
