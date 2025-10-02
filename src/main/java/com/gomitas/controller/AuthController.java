package com.gomitas.controller;

import com.gomitas.dto.AuthDtos;
import com.gomitas.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación y autorización")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Autentica a un usuario y devuelve el token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponseDto> authenticateUser(@Valid @RequestBody AuthDtos.LoginRequestDto loginRequest) {
        log.info("Intento de login para usuario: {}", loginRequest.getNombreUsuario());

        try {
            AuthDtos.AuthResponseDto response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en login para usuario {}: {}", loginRequest.getNombreUsuario(), e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Registrar usuario", description = "Registra un nuevo cliente en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Usuario o email ya existe")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthDtos.AuthResponseDto> registerUser(@Valid @RequestBody AuthDtos.RegisterRequestDto signUpRequest) {
        log.info("Intento de registro para usuario: {}", signUpRequest.getNombreUsuario());

        try {
            AuthDtos.AuthResponseDto response = authService.register(signUpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error en registro para usuario {}: {}", signUpRequest.getNombreUsuario(), e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Obtener información del usuario actual",
            description = "Devuelve la información del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/me")
    public ResponseEntity<AuthDtos.UserInfoDto> getCurrentUser(Authentication authentication) {
        log.debug("Obteniendo información del usuario actual");

        AuthDtos.UserInfoDto userInfo = authService.getCurrentUser(authentication);
        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "Renovar token", description = "Genera un nuevo token usando el refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido"),
            @ApiResponse(responseCode = "401", description = "Refresh token expirado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthDtos.AuthResponseDto> refreshToken(@Valid @RequestBody AuthDtos.RefreshTokenRequestDto request) {
        log.debug("Renovando token");

        try {
            AuthDtos.AuthResponseDto response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error renovando token: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Verificar disponibilidad de nombre de usuario")
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameAvailability(@RequestParam String username) {
        boolean exists = authService.existsByNombreUsuario(username);
        return ResponseEntity.ok(!exists); // Devuelve true si está disponible
    }

    @Operation(summary = "Verificar disponibilidad de email")
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email) {
        boolean exists = authService.existsByCorreo(email);
        return ResponseEntity.ok(!exists); // Devuelve true si está disponible
    }
}