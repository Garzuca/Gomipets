package com.gomitas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductoDtos {

    @Builder
    public record ProductoResponseDto(
            Long productoId,
            String nombre,
            String descripcion,
            BigDecimal precioUnitario,
            Integer stockMinimo,
            LocalDate fechaCreacion,
            Boolean estado
    ) {}

    @Builder
    public record CreateProductoRequestDto(
            @NotBlank(message = "El nombre del producto es requerido.")
            @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres.")
            String nombre,

            String descripcion,

            @NotNull(message = "El precio unitario es requerido.")
            @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero.")
            BigDecimal precioUnitario,

            Integer stockMinimo
    ) {}

    @Builder
    public record UpdateProductoRequestDto(
            @NotBlank(message = "El nombre del producto es requerido.")
            @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres.")
            String nombre,

            String descripcion,

            @NotNull(message = "El precio unitario es requerido.")
            @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero.")
            BigDecimal precioUnitario,

            Integer stockMinimo,

            Boolean estado
    ) {}
}
