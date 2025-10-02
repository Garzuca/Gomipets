package com.gomitas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class ClienteDtos {

    @Builder
    public record ClienteResponseDto(
            Long clienteId,
            Long usuarioId,
            String nombreUsuario,
            String correo,
            String nombre,
            String direccion,
            String telefono,
            String tipoMascota
    ) {}

    @Builder
    public record ClienteUpdateDto(
            @NotBlank(message = "El nombre es requerido")
            @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
            String nombre,

            String direccion,
            String telefono,
            String tipoMascota
    ) {}

    @Builder
    public record ClienteCreateDto(
            Long usuarioId,

            @NotBlank(message = "El nombre es requerido")
            @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
            String nombre,

            String direccion,
            String telefono,
            String tipoMascota
    ) {}
}
