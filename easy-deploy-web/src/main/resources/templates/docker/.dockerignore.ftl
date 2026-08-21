# ==============================================================================
# .dockerignore — Bỏ qua file rác, file nhạy cảm và thư viện máy local
# Tối ưu tốc độ build & Bảo mật thông tin
# ==============================================================================

# Thư mục build và thư viện local
target/
*/target/
build/
*/build/
node_modules/
.gradle/
!gradle/wrapper/gradle-wrapper.jar
!gradle/wrapper/gradle-wrapper.properties
__pycache__/
*.pyc
*.pyo
*.pyd

# Mã nguồn Git
.git/
.gitignore
.dockerignore

# File nhạy cảm & môi trường
.env
*.env.local
*.pem
*.key
*.crt

# File rác & logs
*.log
.DS_Store
Thumbs.db
tmp/

# IDE & Editor configs
.idea/
.vscode/
*.iml
*.swp
