export const TECH_STACKS = [
  { value: 'JAVA_MAVEN',     label: 'Java + Maven' },
  { value: 'JAVA_GRADLE',    label: 'Java + Gradle' },
  { value: 'NODE_FRONTEND',  label: 'Node.js (Frontend)' },
  { value: 'NODE_BACKEND',   label: 'Node.js (Backend)' },
  { value: 'PYTHON',         label: 'Python' },
];

export const DB_TYPES = [
  { value: 'POSTGRESQL', label: 'PostgreSQL',  defaultPort: 5432 },
  { value: 'MYSQL',      label: 'MySQL',       defaultPort: 3306 },
  { value: 'MARIADB',    label: 'MariaDB',     defaultPort: 3306 },
  { value: 'MONGODB',    label: 'MongoDB',     defaultPort: 27017 },
  { value: 'REDIS',      label: 'Redis',       defaultPort: 6379 },
  { value: 'NONE',       label: 'Không dùng',  defaultPort: 0 },
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
  dockerImageTag: '',
  deployMode: 'remote_build', // 'remote_build' hoặc 'registry_pull'
  adminEmail: '',

  envVars: {},
};
