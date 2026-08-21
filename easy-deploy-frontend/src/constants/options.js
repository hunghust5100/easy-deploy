/**
 * Fallback Enums dùng khi chưa nhận được response từ API /api/v1/enums
 */
export const FALLBACK_ENUMS = {
  techStacks: [
    { value: 'JAVA_MAVEN',        label: '☕ Java Spring Boot (Maven)', defaultVersion: '21', defaultPort: 8080 },
    { value: 'JAVA_GRADLE',       label: '🐘 Java Spring Boot (Gradle)', defaultVersion: '21', defaultPort: 8080 },
    { value: 'NODE_FRONTEND',     label: '⚛️ Node.js Frontend (React / Vite / Vue)', defaultVersion: '20', defaultPort: 3000 },
    { value: 'NODE_BACKEND',      label: '🟢 Node.js Backend (Express / NestJS / Fastify)', defaultVersion: '20', defaultPort: 3000 },
    { value: 'NEXTJS_FULLSTACK',  label: '⚡ Next.js (SSR / Fullstack Standalone)', defaultVersion: '20', defaultPort: 3000 },
    { value: 'PYTHON',            label: '🐍 Python (FastAPI / Django / Flask)', defaultVersion: '3.11', defaultPort: 8000 },
    { value: 'GO',                label: '🐹 Go (Golang Static Binary)', defaultVersion: '1.22', defaultPort: 8080 },
    { value: 'RUST',              label: '🦀 Rust (Cargo Release)', defaultVersion: '1.78', defaultPort: 8080 },
    { value: 'PHP_LARAVEL',       label: '🐘 PHP (Laravel / Symfony / Composer)', defaultVersion: '8.2', defaultPort: 8000 },
    { value: 'DOTNET',            label: '🔷 .NET / C# (ASP.NET Core 8.0)', defaultVersion: '8.0', defaultPort: 8080 },
    { value: 'RUBY_RAILS',        label: '💎 Ruby on Rails (Puma Web Server)', defaultVersion: '3.3', defaultPort: 3000 },
  ],
  dbTypes: [
    { value: 'POSTGRESQL', label: '🐘 PostgreSQL',  defaultPort: 5432 },
    { value: 'MYSQL',      label: '🐬 MySQL 8.0',   defaultPort: 3306 },
    { value: 'MARIADB',    label: '🦭 MariaDB',     defaultPort: 3306 },
    { value: 'MONGODB',    label: '🍃 MongoDB',     defaultPort: 27017 },
    { value: 'REDIS',      label: '⚡ Redis Cache', defaultPort: 6379 },
    { value: 'NONE',       label: '🚫 Không dùng Database', defaultPort: 0 },
  ],
  deployModes: [
    {
      value: 'REMOTE_BUILD',
      label: '🖥️ Remote Build trên VPS',
      desc: 'Mã nguồn được tải lên VPS và build Docker image trực tiếp tại VPS (Cần VPS RAM ≥ 2GB).',
    },
    {
      value: 'REGISTRY_PULL',
      label: '🐳 Docker Hub Registry Pull (Khuyên dùng)',
      desc: 'Đóng gói Image lên Docker Hub, VPS chỉ kéo Image về chạy (Siêu nhanh, bảo mật source code, tiết kiệm RAM cho VPS nhỏ 512MB-1GB).',
    },
  ],
};

export const TECH_STACKS = FALLBACK_ENUMS.techStacks;
export const DB_TYPES = FALLBACK_ENUMS.dbTypes;
export const DEPLOY_MODES = FALLBACK_ENUMS.deployModes;

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
  enableCicd: false,
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
  deployMode: 'REMOTE_BUILD',
  adminEmail: '',

  envVars: {},
  services: [],
};
