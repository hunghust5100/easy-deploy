import { useState } from 'react';
import { FolderGit2, Plus, Trash2, Edit3, Globe, Database, Server, RefreshCw } from 'lucide-react';
import { useConfig } from '../../context/ConfigContext';
import './ProjectsPage.css';

function ProjectsPage() {
  const { savedProjects, projectsLoading, loadProjectFromDb, deleteProjectFromDb, refreshProjects, setActiveTab } = useConfig();
  const [deletingId, setDeletingId] = useState(null);

  const handleDelete = async (id, name, e) => {
    e.stopPropagation();
    if (window.confirm(`Bạn có chắc muốn xóa dự án "${name}" khỏi CSDL không?`)) {
      setDeletingId(id);
      try {
        await deleteProjectFromDb(id);
      } finally {
        setDeletingId(null);
      }
    }
  };

  const handleEditAndLoad = async (id) => {
    await loadProjectFromDb(id);
  };

  return (
    <div className="projects-page">
      <div className="projects-page__header">
        <div>
          <h2 className="projects-page__title">
            <FolderGit2 className="text-primary" size={24} />
            Dự Án Đã Lưu
          </h2>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button
            type="button"
            className="btn-action btn-action--secondary"
            onClick={() => refreshProjects()}
            disabled={projectsLoading}
          >
            <RefreshCw size={14} className={projectsLoading ? 'spin' : ''} />
            Làm mới
          </button>
          <button
            type="button"
            className="btn-action btn-action--primary"
            onClick={() => setActiveTab('generator')}
          >
            <Plus size={16} />
            Tạo Cấu Hình
          </button>
        </div>
      </div>

      {savedProjects.length === 0 ? (
        <div className="empty-state">
          <FolderGit2 size={48} className="empty-state__icon" />
          <h3>Chưa có dự án nào</h3>
          <p style={{ marginTop: '0.5rem', marginBottom: '1.5rem' }}>
            Tạo cấu hình trong tab Tạo Cấu Hình và lưu để quản lý tại đây.
          </p>
          <button
            type="button"
            className="btn-action btn-action--primary"
            style={{ margin: '0 auto' }}
            onClick={() => setActiveTab('generator')}
          >
            Tạo Cấu Hình Ngay
          </button>
        </div>
      ) : (
        <div className="projects-grid">
          {savedProjects.map((project) => (
            <div key={project.id} className="project-card">
              <div>
                <div className="project-card__top">
                  <div>
                    <h3 className="project-card__name">{project.appName}</h3>
                    {project.repoUrl && (
                      <span className="project-card__repo">
                        <FolderGit2 size={12} />
                        {project.repoUrl}
                      </span>
                    )}
                  </div>
                  <span className="project-card__badge">{project.techStack}</span>
                </div>

                <div className="project-card__details">
                  <span className="project-card__detail-item">
                    Port: <strong>{project.appPort} : {project.hostPort}</strong>
                  </span>
                  {project.dbType && project.dbType !== 'NONE' && (
                    <span className="project-card__detail-item" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <Database size={12} /> {project.dbType}
                    </span>
                  )}
                  {project.enableNginx && (
                    <span className="project-card__detail-item" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <Globe size={12} /> {project.domainName || 'Nginx'}
                    </span>
                  )}
                  {project.serverName && (
                    <span className="project-card__detail-item" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <Server size={12} /> {project.serverName}
                    </span>
                  )}
                </div>
              </div>

              <div className="project-card__actions">
                <button
                  type="button"
                  className="btn-action btn-action--primary"
                  onClick={() => handleEditAndLoad(project.id)}
                >
                  <Edit3 size={14} />
                  Mở & Chỉnh sửa
                </button>
                <button
                  type="button"
                  className="btn-action btn-action--danger"
                  title="Xóa dự án"
                  onClick={(e) => handleDelete(project.id, project.appName, e)}
                  disabled={deletingId === project.id}
                >
                  <Trash2 size={14} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default ProjectsPage;
