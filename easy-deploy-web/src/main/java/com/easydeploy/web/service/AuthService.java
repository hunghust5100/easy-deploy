package com.easydeploy.web.service;

import com.easydeploy.web.dto.request.LoginRequest;
import com.easydeploy.web.dto.request.RegisterRequest;
import com.easydeploy.web.dto.response.UserResponse;
import com.easydeploy.web.entity.UserEntity;
import com.easydeploy.web.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập địa chỉ Email");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập Mật khẩu");
        }

        String email = request.getEmail().trim().toLowerCase();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc Mật khẩu không chính xác"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("Email hoặc Mật khẩu không chính xác");
        }

        if ("INACTIVE".equalsIgnoreCase(user.getStatus()) || "BLOCKED".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("Tài khoản của bạn đã bị tạm khóa");
        }

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập Họ và Tên");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập địa chỉ Email");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email này đã được sử dụng. Vui lòng chọn Email khác hoặc Đăng nhập");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName().trim());
        user.setRole("DEVELOPER");
        user.setStatus("ACTIVE");

        UserEntity saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        return UserResponse.fromEntity(user);
    }
}
