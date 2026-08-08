import api from './api';

export const generateZip = async (config) => {
  const response = await api.post('/generate', config, {
    responseType: 'blob',
  });

  const url = window.URL.createObjectURL(response.data);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${config.appName || 'app'}-devops-config.zip`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  window.URL.revokeObjectURL(url);
};

export const generateCustomZip = async (files, appName = 'app') => {
  const response = await api.post('/generate-custom', files, {
    responseType: 'blob',
  });

  const url = window.URL.createObjectURL(response.data);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${appName}-devops-config.zip`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  window.URL.revokeObjectURL(url);
};
