# Easy Deploy Tool (`easy-deploy`) 🚀

**Easy Deploy** là giải pháp công cụ hỗ trợ tự động hóa sinh file cấu hình DevOps (Dockerfile, docker-compose, Nginx, GitHub Actions CI/CD), tích hợp tính năng **1-Click SSH Deploy** lên máy chủ VPS và **Web SSH Terminal** trực tiếp trên trình duyệt.

---

## 🌟 Tính năng Nổi bật

* 🔍 **Smart Multi-Module Tech Stack Detector:** Tự động phân tích cây thư mục mã nguồn (Local hoặc qua GitHub URL) để nhận diện tất cả các packages/modules (Frontend React/Vite, Backend Java/Spring Boot, Python, Go, Node.js...).
* 🧩 **Interactive Service Selector:** Cho phép người dùng trực quan lựa chọn và tùy biến cấu hình triển khai riêng cho từng module (port, container name, biến môi trường) hoặc triển khai toàn bộ hệ thống Full-stack chỉ với 1 cú click.
* 🛠️ **DevOps Config Generator:** Sinh bộ file cấu hình tiêu chuẩn sản xuất:
  * `Dockerfile` tối ưu multi-stage build độc lập cho từng module.
  * `docker-compose.yml` ghép nối đa dịch vụ ứng dụng với các dịch vụ bổ trợ (PostgreSQL, MySQL, Redis, Nginx Gateway).
  * `nginx.conf` cấu hình Smart Reverse Proxy tự động định tuyến `/` sang Frontend và `/api` sang Backend.
  * `.github/workflows/deploy-github.yml` quy trình CI/CD hoàn chỉnh tự động build & push Docker Image và SSH deploy lên VPS.
* 🚀 **1-Click SSH Deploy:** Tải file cấu hình, kết nối SSH/SFTP lên máy chủ từ xa, khởi chạy container và stream log thời gian thực về giao diện Web qua WebSocket.
* 💻 **Web SSH Terminal:** Trình quản lý Terminal tương tác trực tiếp trên trình duyệt tích hợp `@xterm/xterm`.
* 📋 **Hướng dẫn Cấu hình Bắt buộc (Manual Setup Guide):** Loại bỏ các bước triển khai thủ công rườm rà (vốn đã được tự động hóa), tập trung hướng dẫn người dùng cấu hình 4 hạng mục bắt buộc ngoài hệ thống:
  1. **GitHub Secrets:** Khai báo thông tin bảo mật CI/CD (`DOCKERHUB_TOKEN`, `SERVER_HOST`, `SERVER_USER`...).
  2. **Vercel / Netlify:** Triển khai Frontend độc lập và kết nối API Backend (`VITE_API_BASE_URL`).
  3. **VPS Firewall & DNS:** Mở các port giao tiếp (`80`, `443`, `22`), trỏ tên miền (A Record) về IP VPS.
  4. **Biến Môi trường Production (.env):** Khai báo mật khẩu Database, JWT Secret Key trên server VPS.

---

## 🏗️ Cấu trúc Kiến trúc Monorepo Multi-Module

Dự án được chuẩn hóa thành cấu trúc Monorepo mạch lạc:

```text
easy-deploy/
├── easy-deploy-core/          # Submodule 1: Thư viện Lõi Pure Java 25 (Detector, FreeMarker Engine, Exporter)
├── easy-deploy-cli/           # Submodule 2: Ứng dụng CLI Tool (Picocli + JLine Interactive Wizard)
├── easy-deploy-web/           # Submodule 3: Backend REST API & WebSocket Server (Spring Boot 3.4 + JSch SSH)
├── easy-deploy-frontend/      # Submodule 4: Giao diện Web Client (React + Vite + Tailwind CSS + XTerm)
├── build.gradle               # Gradle Multi-Project configuration
├── settings.gradle
└── README.md
```

---

## 🚀 Hướng dẫn Biên dịch & Kích chạy

### 1. Yêu cầu Môi trường
* **Java SDK:** Java 17 trở lên (Hỗ trợ tốt nhất trên Java 21 / 25).
* **Node.js:** Node 18+ và `npm`.

---

### 2. Khởi chạy Ứng dụng Web UI (Frontend + Backend)

#### **Bước 2.1: Chạy Backend WebSocket Server (Spring Boot)**
```bash
./gradlew :easy-deploy-web:bootRun
```
* Backend API & WebSocket Server sẽ lắng nghe tại: `http://localhost:8088`.
* Các endpoint WebSocket:
  * `/ws/deploy`: Xử lý 1-Click Deploy & Stream Log.
  * `/ws/ssh`: Xử lý Web SSH Terminal.

#### **Bước 2.2: Chạy Frontend Client (React + Vite)**
Mở một tab terminal khác:
```bash
cd easy-deploy-frontend
npm install   # Nếu chạy lần đầu
npm run dev
```
* Truy cập giao diện ứng dụng tại: `http://localhost:5173`.

---

### 3. Biên dịch & Sử dụng CLI Tool

#### **Biên dịch file `.jar`:**
```bash
./gradlew build
```
File thực thi CLI `.jar` sẽ được sinh ra tại: `easy-deploy-cli/build/libs/easy-deploy-cli-1.0.0-SNAPSHOT.jar`.

#### **Chạy thử CLI Tool:**
* **Tự động quét thư mục & sinh cấu hình:**
  ```bash
  java -jar easy-deploy-cli/build/libs/easy-deploy-cli-1.0.0-SNAPSHOT.jar scan
  ```
* **Chế độ Tương tác (Interactive Wizard):**
  ```bash
  java -jar easy-deploy-cli/build/libs/easy-deploy-cli-1.0.0-SNAPSHOT.jar init
  ```

---

## 🧪 Kiểm thử (Testing)

Khởi chạy toàn bộ unit tests cho các module:
```bash
./gradlew test
```

