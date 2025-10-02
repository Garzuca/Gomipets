package com.gomitas.dto;

import com.gomitas.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

public class AuthDtos {

    @Data
    @Builder
    public static class LoginRequestDto {
        @NotBlank(message = "El nombre de usuario es requerido")
        private String nombreUsuario;

        @NotBlank(message = "La contraseña es requerida")
        private String contraseña;
    }

    @Data
    @Builder
    public static class RegisterRequestDto {
        @NotBlank(message = "El nombre de usuario es requerido")
        @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
        private String nombreUsuario;

        @Email(message = "Debe ser un email válido")
        private String correo;

        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        private String contraseña;

        // Datos del cliente
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        private String nombre;

        private String direccion;
        private String telefono;
        private String tipoMascota;
    }

    @Data
    @Builder
    public static class AuthResponseDto {
        private String token;
        private String refreshToken;
        @Builder.Default
        private String tipo = "Bearer";
        private Long usuarioId;
        private String nombreUsuario;
        private String correo;
        private Rol rol;
        private LocalDate fechaRegistro;
    }

    @Data
    @Builder
    public static class UserInfoDto {
        private Long usuarioId;
        private String nombreUsuario;
        private String correo;
        private Rol rol;
        private LocalDate fechaRegistro;
        private Boolean estado;
    }

    public record RefreshTokenRequestDto(
            @NotBlank(message = "El refresh token es requerido")
            String refreshToken
    ) {}
}
