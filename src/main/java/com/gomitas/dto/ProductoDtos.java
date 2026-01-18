package com.gomitas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;

public class ProductoDtos {

    @Builder
    public record ProductoResponseDto(
            Long productoId,
            String nombre,
            String descripcion,
            BigDecimal precioUnitario,
            String imageUrl,
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

    @Builder
    public record RecetaInsumoDto(
            @NotNull Long insumoId,
            @NotNull @DecimalMin("0.0001") BigDecimal cantidadRequerida
    ) {}

    @Builder
    public record CreateRecetaRequestDto(
            @NotNull
            @Size(min = 1, message = "La receta debe contener al menos un insumo.")
            List<@Valid RecetaInsumoDto> insumos
    ) {}

    @Builder
    public record RecetaResponseDto(
            Long insumoId,
            String nombreInsumo,
            BigDecimal cantidadRequerida,
            String unidad
    ) {}
}
