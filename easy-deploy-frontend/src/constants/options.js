export const TECH_STACKS = [
  { value: 'JAVA_MAVEN',     label: '☕ Java Spring Boot (Maven)' },
  { value: 'JAVA_GRADLE',    label: '🐘 Java Spring Boot (Gradle)' },
  { value: 'NODE_FRONTEND',  label: '⚛️ Node.js Frontend (React / Vite / Next.js / Vue)' },
  { value: 'NODE_BACKEND',   label: '🟢 Node.js Backend (Express / NestJS / Fastify)' },
  { value: 'PYTHON',         label: '🐍 Python (FastAPI / Django / Flask)' },
  { value: 'GO',             label: '🐹 Go (Golang)' },
  { value: 'RUST',           label: '🦀 Rust' },
];

export const DB_TYPES = [
  { value: 'POSTGRESQL', label: '🐘 PostgreSQL',  defaultPort: 5432 },
  { value: 'MYSQL',      label: '🐬 MySQL 8.0',   defaultPort: 3306 },
  { value: 'MARIADB',    label: '🦭 MariaDB',     defaultPort: 3306 },
  { value: 'MONGODB',    label: '🍃 MongoDB',     defaultPort: 27017 },
  { value: 'REDIS',      label: '⚡ Redis Cache', defaultPort: 6379 },
  { value: 'NONE',       label: '🚫 Không dùng Database', defaultPort: 0 },
];

export const DEPLOY_MODES = [
  {
    value: 'remote_build',
    label: '🖥️ Remote Build trên VPS',
    desc: 'Mã nguồn được tải lên VPS và build Docker image trực tiếp tại VPS (Cần VPS RAM ≥ 2GB).',
  },
  {
    value: 'registry_pull',
    label: '🐳 Docker Hub Registry Pull (Khuyên dùng)',
    desc: 'Đóng gói Image lên Docker Hub, VPS chỉ kéo Image về chạy (Siêu nhanh, bảo mật source code, tiết kiệm RAM cho VPS nhỏ 512MB-1GB).',
  },
];

export const DEFAULT_CONFIG = {
  appName: 'my-app',
  techStack: 'JAVA_MAVEN',
  techVersion: '21',
  appPort: 8080,
  hostPort: 8080,
  dbType: 'POSTGRESQL',
  dbName: 'app_db',
  dbUser: 'postgres',
  dbPass: 'secret',
  dbPort: 5432,
  enableNginx: true,
  domainName: 'localhost',
  enableCicd: true,
  dockerHubUser: '',
  gitBranch: 'main',
  deployPath: '/root/my-app',

  /* Tùy chọn Setup Server & Docker Hub Deployment */
  enableServerSetup: false,
  installNginx: false,
  installCertbot: false,
  setupFirewall: false,
  installDocker: false,
  useSslipIo: false,

  useDockerHub: false,
  dockerHubUsername: '',
  dockerHubToken: '',
  dockerImageTag: 'latest',
  deployMode: 'remote_build', // 'remote_build' hoặc 'registry_pull'
  adminEmail: '',

  envVars: {},
};
