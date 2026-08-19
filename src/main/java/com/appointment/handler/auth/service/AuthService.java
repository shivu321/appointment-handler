package com.appointment.handler.auth.service;

import com.appointment.handler.auth.dto.AuthResponse;
import com.appointment.handler.auth.dto.LoginRequest;
import com.appointment.handler.auth.dto.RegisterRequest;
import com.appointment.handler.auth.entity.Role;
import com.appointment.handler.auth.entity.User;
import com.appointment.handler.auth.repository.RoleRepository;
import com.appointment.handler.auth.repository.UserRepository;
import com.appointment.handler.common.enums.UserStatus;
import com.appointment.handler.common.exception.AppException;
import com.appointment.handler.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already in use", "EMAIL_ALREADY_IN_USE", HttpStatus.BAD_REQUEST);
        }

        Set<String> requestRoles = request.getRoles();
        if (requestRoles == null || requestRoles.isEmpty()) {
            requestRoles = Set.of("CUSTOMER");
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : requestRoles) {
            String formattedRole = roleName.toUpperCase().trim();
            Role role = roleRepository.findByName(formattedRole)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(formattedRole).build()));
            roles.add(role);
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception ex) {
            throw new AppException("Invalid email or password", "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException("User account is inactive", "USER_INACTIVE", HttpStatus.FORBIDDEN);
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null) {
            throw new AppException("Invalid refresh token", "INVALID_REFRESH_TOKEN", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("User not found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new AppException("Expired or invalid refresh token", "INVALID_REFRESH_TOKEN", HttpStatus.UNAUTHORIZED);
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
