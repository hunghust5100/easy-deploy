# Easy Deploy (`easy-deploy`) 🚀

**Easy Deploy** là nền tảng tự động hóa nhận diện mã nguồn, sinh trọn bộ cấu hình DevOps chuẩn hóa (Dockerfile đa tầng, Docker Compose, Nginx Reverse Proxy, GitHub Actions CI/CD) và hỗ trợ **Triển khai 1-Click lên VPS qua SSH** cùng **Web SSH Terminal** tương tác trực tiếp trên trình duyệt.

---

## 🌟 Tính năng Nổi bật

* 🔍 **Nhận diện Ngôn ngữ & Framework tự động (Smart Tech Stack Detector):** Quét cây thư mục mã nguồn từ xa qua GitHub API / Zipball stream hoặc thư mục cục bộ, nhận diện chính xác các thành phần: Java Spring Boot (Maven/Gradle), Node.js (React, Vite, Vue, Express, NestJS, Next.js), Python (FastAPI, Django, Flask), Go, Rust, PHP Laravel, .NET, Ruby on Rails...
* 🧩 **Hỗ trợ Fullstack & Monorepo (Interactive Service Selector):** Tự động phân tách Frontend SPA, Backend API và dịch vụ cơ sở dữ liệu con; cho phép tùy biến cổng mạng, biến môi trường và thiết lập Nginx Gateway định tuyến thông minh.
* 🛠️ **Bộ sinh cấu hình DevOps chuẩn hóa (DevOps Config Generator):**
  * `Dockerfile` tối ưu hóa đa tầng (Multi-stage Build), Non-root User bảo mật và tận dụng triệt để Layer Caching giúp giảm 77% – 96% dung lượng Image.
  * `docker-compose.yml` ghép nối đa dịch vụ và container CSDL (PostgreSQL, MySQL, MariaDB, MongoDB, Redis).
  * `nginx.conf` Smart Gateway tự động reverse proxy `/` về Frontend, `/api/` về Backend và `/ws/` về WebSocket Server với timeout tối ưu.
  * `.github/workflows/deploy.yml` Pipeline CI/CD tự động build & push Docker Hub và kích hoạt triển khai VPS.
* 🚀 **Triển khai 1-Click qua SSH (Agentless Architecture):** Tự động khởi tạo môi trường VPS (Docker, Docker Compose, Nginx, UFW), tải cấu hình qua SFTP và khởi chạy container qua kết nối SSH2 tiêu chuẩn mà không cần cài đặt bất kỳ agent nền nào lên máy chủ.
* 💻 **Web SSH Terminal & Live Log Streaming:** Tích hợp `@xterm/xterm` và Spring WebSocket để theo dõi tiến trình triển khai thời gian thực và quản trị máy chủ Linux trực tiếp từ giao diện web.
* 🗄️ **Quản lý Dự án & Máy chủ (Server & Project Management):** Lưu trữ thông tin máy chủ VPS, cấu hình dự án và lịch sử các đợt triển khai trên cơ sở dữ liệu H2 bền vững.

---

## 🏗️ Cấu trúc Dự án

Dự án được tổ chức phân tách rõ ràng giữa tầng giao diện người dùng và máy chủ xử lý:

```text
easy-deploy/
├── easy-deploy-frontend/      # Ứng dụng Giao diện Web SPA (React 19 + Vite + Xterm.js + Lucide)
├── easy-deploy-web/           # Máy chủ Backend tích hợp (Spring Boot 3.4 + Core DevOps Engine + JSch SSH + WebSocket)
│   ├── src/main/java/com/easydeploy/
│   │   ├── core/              # Động cơ lõi: Detector, Generator (FreeMarker), Scanner, Parser, SSH Core
│   │   └── web/               # Tầng Web: Controllers, Services, Repositories, Entities, WebSocket Handlers
│   └── src/main/resources/
│       ├── templates/         # Mẫu cấu hình FreeMarker (Docker, Compose, Nginx, CI/CD, Script)
│       └── application.yml
├── build.gradle               # Cấu hình Gradle Root
├── settings.gradle
└── README.md
```

---

## 🚀 Hướng dẫn Cài đặt & Khởi chạy

### 1. Yêu cầu Hệ thống
* **Java SDK:** Java 17 hoặc Java 21 LTS trở lên.
* **Node.js:** Node.js 18+ và `npm`.

---

### 2. Khởi chạy Ứng dụng

#### **Bước 2.1: Chạy Backend Server (Spring Boot)**
```bash
./gradlew :easy-deploy-web:bootRun
```
* Backend API hoạt động tại: `http://localhost:8088`
* WebSocket Endpoints:
  * `/ws/deploy-logs`: Stream log triển khai thời gian thực.
  * `/ws/ssh-terminal`: Phiên điều khiển Web SSH Terminal.

#### **Bước 2.2: Chạy Frontend Client (React + Vite)**
Mở một tab terminal khác:
```bash
cd easy-deploy-frontend
npm install    # Chạy lần đầu tiên
npm run dev
```
* Truy cập giao diện ứng dụng tại: `http://localhost:5173`.

---

## 🧪 Kiểm thử (Testing)

### Kiểm thử Backend
```bash
./gradlew clean test
```

### Đóng gói Backend JAR
```bash
./gradlew :easy-deploy-web:bootJar
```

### Kiểm thử & Build Frontend
```bash
cd easy-deploy-frontend
npm run build
```
