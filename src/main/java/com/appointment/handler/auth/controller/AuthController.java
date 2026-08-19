package com.appointment.handler.auth.controller;

import com.appointment.handler.auth.dto.AuthResponse;
import com.appointment.handler.auth.dto.LoginRequest;
import com.appointment.handler.auth.dto.RefreshRequest;
import com.appointment.handler.auth.dto.RegisterRequest;
import com.appointment.handler.auth.service.AuthService;
import com.appointment.handler.common.response.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseDto<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseDto.success("User registered successfully", response);
    }

    @PostMapping("/login")
    public ResponseDto<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseDto.success("User logged in successfully", response);
    }

    @PostMapping("/refresh")
    public ResponseDto<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseDto.success("Token refreshed successfully", response);
    }
}
