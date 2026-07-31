# Easy Deploy Tool (`easy-deploy`)

Công cụ hỗ trợ sinh file cấu hình DevOps cơ bản cho ứng dụng Web (Dockerfile, docker-compose, Nginx, GitHub Actions).

## 🏗️ Cấu trúc Kiến trúc Monorepo Multi-Module

Dự án được chuẩn hóa thành cấu trúc Monorepo mạch lạc:

```text
easy-deploy/
├── easy-deploy-core/          # Submodule 1: Thư viện Lõi Pure Java 25 (Detector, FreeMarker, Exporter)
├── easy-deploy-cli/           # Submodule 2: Ứng dụng CLI Tool (Picocli + JLine)
├── easy-deploy-frontend/      # Submodule 3: Giao diện Web (React + Vite + Tailwind CSS)
├── build.gradle               # Gradle Multi-Project config
├── settings.gradle
└── README.md
```

## 🚀 Hướng dẫn Biên dịch & Sử dụng

### 1. Biên dịch CLI Tool (Java 25)
```bash
./gradlew build
```
File thực thi CLI `.jar` sẽ được tạo tại: `easy-deploy-cli/build/libs/easy-deploy-cli-1.0.0-SNAPSHOT.jar`.

### 2. Chạy thử CLI Tool

* **Tự động quét thư mục & sinh cấu hình:**
  ```bash
  java -jar easy-deploy-cli/build/libs/easy-deploy-cli-1.0.0-SNAPSHOT.jar scan
  ```

* **Chế độ Tương tác (Interactive Wizard):**
  ```bash
  java -jar easy-deploy-cli/build/libs/easy-deploy-cli-1.0.0-SNAPSHOT.jar init
  ```

### 3. Chạy thử Frontend Web UI
```bash
cd easy-deploy-frontend
npm run dev
```
Giao diện sẽ chạy tại `http://localhost:5173`.
