import { useState } from 'react';
import { Server, Plus, CheckCircle, XCircle, RefreshCw, Terminal, Trash2, Edit3, Key, Shield, Wifi, X } from 'lucide-react';
import { useVps } from '../../context/VpsContext';
import { useConfig } from '../../context/ConfigContext';
import './ServersPage.css';

function ServersPage() {
  const { vpsList, loading, saveVpsProfile, deleteVpsProfile, testConnection, refreshServers, setActiveVpsId } = useVps();
  const { setActiveTab } = useConfig();

  const [modalOpen, setModalOpen] = useState(false);
  const [editingServer, setEditingServer] = useState(null);
  const [testingId, setTestingId] = useState(null);
  const [testResults, setTestResults] = useState({});

  const [formData, setFormData] = useState({
    name: '',
    host: '',
    sshPort: 22,
    sshUser: 'root',
    authType: 'PASSWORD',
    password: '',
    privateKey: '',
    defaultDeployPath: '/root',
  });

  const openAddModal = () => {
    setEditingServer(null);
    setFormData({
      name: '',
      host: '',
      sshPort: 22,
      sshUser: 'root',
      authType: 'PASSWORD',
      password: '',
      privateKey: '',
      defaultDeployPath: '/root',
    });
    setModalOpen(true);
  };

  const openEditModal = (server) => {
    setEditingServer(server);
    setFormData({
      name: server.name || '',
      host: server.host || '',
      sshPort: server.sshPort || 22,
      sshUser: server.sshUser || 'root',
      authType: server.authType || 'PASSWORD',
      password: server.password || '',
      privateKey: server.privateKey || '',
      defaultDeployPath: server.defaultDeployPath || '/root',
    });
    setModalOpen(true);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    try {
      await saveVpsProfile({
        ...formData,
        id: editingServer?.id,
      });
      setModalOpen(false);
    } catch (err) {
      alert('Lỗi lưu máy chủ: ' + err.message);
    }
  };

  const handleTestConnection = async (id) => {
    setTestingId(id);
    setTestResults((prev) => ({ ...prev, [id]: null }));
    try {
      const res = await testConnection(id);
      setTestResults((prev) => ({ ...prev, [id]: res?.connected ? 'success' : 'failed' }));
    } catch {
      setTestResults((prev) => ({ ...prev, [id]: 'failed' }));
    } finally {
      setTestingId(null);
    }
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`Bạn có chắc muốn xóa máy chủ VPS "${name}" không?`)) {
      await deleteVpsProfile(id);
    }
  };

  const handleOpenTerminal = (server) => {
    setActiveVpsId(server.id);
    setActiveTab('terminal');
  };

  return (
    <div className="servers-page">
      <div className="servers-page__header">
        <div>
          <h2 className="servers-page__title">
            <Server className="text-primary" size={24} />
            Máy Chủ VPS
          </h2>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button
            type="button"
            className="btn-action btn-action--secondary"
            onClick={() => refreshServers()}
            disabled={loading}
          >
            <RefreshCw size={14} className={loading ? 'spin' : ''} />
            Làm mới
          </button>
          <button
            type="button"
            className="btn-action btn-action--primary"
            onClick={openAddModal}
          >
            <Plus size={16} />
            Thêm Máy Chủ
          </button>
        </div>
      </div>

      {vpsList.length === 0 ? (
        <div className="empty-state">
          <Server size={48} className="empty-state__icon" />
          <h3>Chưa có máy chủ VPS nào</h3>
          <p style={{ marginTop: '0.5rem', marginBottom: '1.5rem' }}>
            Thêm thông tin máy chủ VPS để triển khai ứng dụng và mở Web Terminal.
          </p>
          <button
            type="button"
            className="btn-action btn-action--primary"
            style={{ margin: '0 auto' }}
            onClick={openAddModal}
          >
            Thêm Máy Chủ
          </button>
        </div>
      ) : (
        <div className="servers-grid">
          {vpsList.map((server) => {
            const testStatus = testResults[server.id];
            const isTesting = testingId === server.id;

            return (
              <div key={server.id} className="server-card">
                <div>
                  <div className="server-card__top">
                    <div>
                      <h3 className="server-card__name">{server.name}</h3>
                      <div className="server-card__host">
                        {server.sshUser}@{server.host}:{server.sshPort}
                      </div>
                    </div>
                    {testStatus && (
                      <span className={`status-badge ${testStatus === 'success' ? 'status-badge--success' : 'status-badge--danger'}`}>
                        {testStatus === 'success' ? <CheckCircle size={12} /> : <XCircle size={12} />}
                        {testStatus === 'success' ? 'Kết nối Tốt' : 'Không kết nối được'}
                      </span>
                    )}
                  </div>

                  <div className="server-card__info-row">
                    <Shield size={14} className="text-muted" />
                    <span>Xác thực: <strong>{server.authType}</strong></span>
                  </div>
                  <div className="server-card__info-row">
                    <Key size={14} className="text-muted" />
                    <span>Thư mục triển khai: <code>{server.defaultDeployPath || '/root'}</code></span>
                  </div>
                </div>

                <div className="server-card__actions">
                  <button
                    type="button"
                    className="btn-action btn-action--secondary"
                    onClick={() => handleTestConnection(server.id)}
                    disabled={isTesting}
                  >
                    <Wifi size={14} className={isTesting ? 'spin' : ''} />
                    {isTesting ? 'Đang Test...' : 'Test SSH'}
                  </button>

                  <button
                    type="button"
                    className="btn-action btn-action--primary"
                    onClick={() => handleOpenTerminal(server)}
                  >
                    <Terminal size={14} />
                    Mở Terminal
                  </button>

                  <button
                    type="button"
                    className="btn-action btn-action--secondary"
                    onClick={() => openEditModal(server)}
                    title="Chỉnh sửa"
                  >
                    <Edit3 size={14} />
                  </button>

                  <button
                    type="button"
                    className="btn-action btn-action--danger"
                    onClick={() => handleDelete(server.id, server.name)}
                    title="Xóa máy chủ"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modal Thêm / Sửa Server */}
      {modalOpen && (
        <div className="modal-overlay" onClick={() => setModalOpen(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 style={{ margin: 0, fontSize: '1.2rem', color: '#fff' }}>
                {editingServer ? 'Chỉnh Sửa Máy Chủ VPS' : 'Thêm Máy Chủ VPS Mới'}
              </h3>
              <button type="button" className="btn-action btn-action--danger" onClick={() => setModalOpen(false)}>
                <X size={16} />
              </button>
            </div>

            <form onSubmit={handleFormSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Tên Gợi Nhớ Máy Chủ *</label>
                  <input
                    type="text"
                    required
                    className="form-control"
                    placeholder="VD: VPS Production Singapore"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '0.75rem' }}>
                  <div className="form-group">
                    <label>Địa Chỉ IP / Host VPS *</label>
                    <input
                      type="text"
                      required
                      className="form-control"
                      placeholder="103.179.x.x hoặc vps.domain.com"
                      value={formData.host}
                      onChange={(e) => setFormData({ ...formData, host: e.target.value })}
                    />
                  </div>

                  <div className="form-group">
                    <label>Port SSH</label>
                    <input
                      type="number"
                      className="form-control"
                      value={formData.sshPort}
                      onChange={(e) => setFormData({ ...formData, sshPort: parseInt(e.target.value) || 22 })}
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                  <div className="form-group">
                    <label>SSH Username</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.sshUser}
                      onChange={(e) => setFormData({ ...formData, sshUser: e.target.value })}
                    />
                  </div>

                  <div className="form-group">
                    <label>Kiểu Xác Thực</label>
                    <select
                      className="form-control"
                      value={formData.authType}
                      onChange={(e) => setFormData({ ...formData, authType: e.target.value })}
                    >
                      <option value="PASSWORD">Mật khẩu (Password)</option>
                      <option value="SSH_KEY">Khóa SSH (Private Key)</option>
                    </select>
                  </div>
                </div>

                {formData.authType === 'PASSWORD' ? (
                  <div className="form-group">
                    <label>Mật Khẩu SSH</label>
                    <input
                      type="password"
                      className="form-control"
                      placeholder="Nhập mật khẩu VPS root"
                      value={formData.password}
                      onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    />
                  </div>
                ) : (
                  <div className="form-group">
                    <label>Nội dung SSH Private Key (RSA / Ed25519)</label>
                    <textarea
                      rows={4}
                      className="form-control"
                      placeholder="-----BEGIN OPENSSH PRIVATE KEY-----..."
                      value={formData.privateKey}
                      onChange={(e) => setFormData({ ...formData, privateKey: e.target.value })}
                    />
                  </div>
                )}

                <div className="form-group">
                  <label>Thư Mục Triển Khai Mặc Định</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="/root"
                    value={formData.defaultDeployPath}
                    onChange={(e) => setFormData({ ...formData, defaultDeployPath: e.target.value })}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn-action btn-action--secondary" onClick={() => setModalOpen(false)}>
                  Hủy
                </button>
                <button type="submit" className="btn-action btn-action--primary">
                  {editingServer ? 'Lưu Thay Đổi' : 'Tạo Máy Chủ'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default ServersPage;
