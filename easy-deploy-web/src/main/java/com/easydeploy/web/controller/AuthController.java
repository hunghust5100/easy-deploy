package com.easydeploy.web.controller;

import com.easydeploy.web.dto.ApiResponse;
import com.easydeploy.web.dto.request.LoginRequest;
import com.easydeploy.web.dto.request.RegisterRequest;
import com.easydeploy.web.dto.response.UserResponse;
import com.easydeploy.web.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginRequest request) {
        try {
            UserResponse user = authService.login(request);
            return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody RegisterRequest request) {
        try {
            UserResponse user = authService.register(request);
            return ResponseEntity.ok(ApiResponse.ok("Đăng ký tài khoản thành công", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@RequestParam UUID userId) {
        try {
            UserResponse user = authService.getMe(userId);
            return ResponseEntity.ok(ApiResponse.ok(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }
}
