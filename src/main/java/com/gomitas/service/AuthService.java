package com.gomitas.service;

import com.gomitas.dto.AuthDtos;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthDtos.AuthResponseDto login(AuthDtos.LoginRequestDto loginRequest);

    AuthDtos.AuthResponseDto register(AuthDtos.RegisterRequestDto registerRequest);

    AuthDtos.UserInfoDto getCurrentUser(Authentication authentication);

    AuthDtos.AuthResponseDto refreshToken(AuthDtos.RefreshTokenRequestDto refreshRequest);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByCorreo(String correo);
}
