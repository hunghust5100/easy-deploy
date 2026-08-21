import React, { useState } from 'react';
import { useConfig } from '../../context/ConfigContext';
import {
  Boxes,
  CheckCircle2,
  Circle,
  FolderTree,
  ChevronDown,
  ChevronUp,
  Settings,
  Plus,
  Trash2,
  Globe,
  Server,
  Layers,
  Terminal,
} from 'lucide-react';
import './ServiceSelector.css';

export default function ServiceSelector() {
  const { config, toggleService, updateService, addCustomService, removeService, enums } = useConfig();
  const [expandedServiceId, setExpandedServiceId] = useState(null);

  const services = config.services || [];
  if (services.length === 0) {
    return null;
  }

  const enabledCount = services.filter((s) => s.enabled).length;

  const toggleExpand = (id) => {
    setExpandedServiceId((prev) => (prev === id ? null : id));
  };

  const handleAddNewService = () => {
    const newId = `custom-service-${Date.now().toString().slice(-4)}`;
    const newService = {
      id: newId,
      name: `service-${services.length + 1}`,
      relativePath: `./service-${services.length + 1}`,
      serviceType: 'BACKEND',
      techStack: 'NODE_BACKEND',
      techVersion: '20',
      containerPort: 3000,
      hostPort: 3000 + services.length,
      buildCommand: 'npm run build',
      enabled: true,
      envVars: {},
    };
    addCustomService(newService);
    setExpandedServiceId(newId);
  };

  const getServiceTypeBadge = (type) => {
    const t = (type || 'BACKEND').toUpperCase();
    if (t === 'FRONTEND') {
      return <span className="service-type-badge service-type-badge--frontend">Frontend SPA</span>;
    }
    if (t === 'FULLSTACK') {
      return <span className="service-type-badge service-type-badge--fullstack">Fullstack / SSR</span>;
    }
    if (t === 'DATABASE') {
      return <span className="service-type-badge service-type-badge--database">Database</span>;
    }
    return <span className="service-type-badge service-type-badge--backend">Backend API</span>;
  };

  const getServiceIcon = (type) => {
    const t = (type || 'BACKEND').toUpperCase();
    if (t === 'FRONTEND') return <Globe size={18} className="text-sky-500" />;
    if (t === 'FULLSTACK') return <Layers size={18} className="text-purple-500" />;
    return <Server size={18} className="text-emerald-500" />;
  };

  return (
    <div className="service-selector">
      <div className="service-selector__header">
        <div>
          <div className="service-selector__title-wrap">
            <h3 className="service-selector__title">
              <Boxes size={20} className="text-indigo-600" />
              <span>Dịch Vụ & Packages Nhận Diện Được</span>
            </h3>
            <span className="service-selector__badge">
              {enabledCount}/{services.length} Đang kích hoạt
            </span>
          </div>
          <p className="service-selector__subtitle">
            Hệ thống đã phân tích và tìm thấy các packages con trong repository. Bạn có thể bật/tắt hoặc tùy biến cấu hình từng service.
          </p>
        </div>
      </div>

      <div className="service-selector__list">
        {services.map((service) => {
          const isExpanded = expandedServiceId === service.id;
          const isEnabled = service.enabled !== false;

          return (
            <div
              key={service.id}
              className={`service-card ${isEnabled ? 'service-card--enabled' : 'service-card--disabled'}`}
            >
              <div className="service-card__main">
                <div className="service-card__left">
                  <div className="service-card__checkbox-wrap">
                    <input
                      type="checkbox"
                      id={`svc-check-${service.id}`}
                      className="service-card__checkbox"
                      checked={isEnabled}
                      onChange={() => toggleService(service.id)}
                    />
                  </div>

                  {getServiceIcon(service.serviceType)}

                  <div className="service-card__info">
                    <div className="service-card__name-row">
                      <label
                        htmlFor={`svc-check-${service.id}`}
                        className="service-card__name cursor-pointer"
                      >
                        {service.name || service.id}
                      </label>
                      {getServiceTypeBadge(service.serviceType)}
                    </div>
                    <div className="service-card__path">
                      <FolderTree size={13} />
                      <span>{service.relativePath || '.'}</span>
                    </div>
                  </div>
                </div>

                <div className="service-card__right">
                  <span className="service-card__stack-pill">
                    {service.techStack} {service.techVersion ? `(${service.techVersion})` : ''}
                  </span>
                  <span className="service-card__port-pill">
                    Port: {service.hostPort || service.containerPort} ➔ {service.containerPort}
                  </span>

                  <button
                    type="button"
                    className="service-card__btn-toggle"
                    onClick={() => toggleExpand(service.id)}
                    title="Tùy chỉnh thông số service"
                  >
                    <Settings size={14} />
                    {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                  </button>
                </div>
              </div>

              {/* Expandable Configuration Details */}
              {isExpanded && (
                <div className="service-card__details">
                  <div className="service-detail__group">
                    <label className="service-detail__label">Tên Container / Service</label>
                    <input
                      type="text"
                      className="service-detail__input"
                      value={service.name || ''}
                      onChange={(e) => updateService(service.id, { name: e.target.value })}
                    />
                  </div>

                  <div className="service-detail__group">
                    <label className="service-detail__label">Thư mục nguồn (Relative Path)</label>
                    <input
                      type="text"
                      className="service-detail__input"
                      value={service.relativePath || '.'}
                      onChange={(e) => updateService(service.id, { relativePath: e.target.value })}
                    />
                  </div>

                  <div className="service-detail__group">
                    <label className="service-detail__label">Tech Stack</label>
                    <select
                      className="service-detail__input"
                      value={service.techStack || 'JAVA_MAVEN'}
                      onChange={(e) => updateService(service.id, { techStack: e.target.value })}
                    >
                      {(enums.techStacks || []).map((stack) => (
                        <option key={stack.value} value={stack.value}>
                          {stack.label}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="service-detail__group">
                    <label className="service-detail__label">Runtime Version</label>
                    <input
                      type="text"
                      className="service-detail__input"
                      value={service.techVersion || ''}
                      placeholder="e.g. 21, 20, 3.11"
                      onChange={(e) => updateService(service.id, { techVersion: e.target.value })}
                    />
                  </div>

                  <div className="service-detail__group">
                    <label className="service-detail__label">Container Port (Nội bộ)</label>
                    <input
                      type="number"
                      className="service-detail__input"
                      value={service.containerPort || 8080}
                      onChange={(e) => updateService(service.id, { containerPort: parseInt(e.target.value, 10) || 8080 })}
                    />
                  </div>

                  <div className="service-detail__group">
                    <label className="service-detail__label">Host Port (Mở ra VPS)</label>
                    <input
                      type="number"
                      className="service-detail__input"
                      value={service.hostPort || 8080}
                      onChange={(e) => updateService(service.id, { hostPort: parseInt(e.target.value, 10) || 8080 })}
                    />
                  </div>

                  <div className="service-detail__group">
                    <label className="service-detail__label">Lệnh Build tùy chọn</label>
                    <input
                      type="text"
                      className="service-detail__input"
                      value={service.buildCommand || ''}
                      placeholder="e.g. npm run build, ./gradlew bootJar"
                      onChange={(e) => updateService(service.id, { buildCommand: e.target.value })}
                    />
                  </div>

                  {service.id.startsWith('custom-service-') && (
                    <div className="service-detail__group flex justify-end items-end">
                      <button
                        type="button"
                        className="text-red-600 hover:text-red-700 text-xs font-semibold flex items-center gap-1 p-2"
                        onClick={() => removeService(service.id)}
                      >
                        <Trash2 size={14} /> Xóa service này
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>

      <button type="button" className="service-selector__add-btn" onClick={handleAddNewService}>
        <Plus size={16} />
        <span>Thêm Dịch Vụ / Package Tùy Chỉnh Vào Cụm Deploy</span>
      </button>
    </div>
  );
}
