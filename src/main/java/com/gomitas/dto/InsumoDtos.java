package com.gomitas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InsumoDtos {

    /** DTO para mostrar un lote individual */
    public record LoteDto(
            Long loteId,
            String numeroLote,
            BigDecimal cantidad,
            BigDecimal costoUnitario,
            LocalDate fechaIngreso,
            LocalDate fechaVencimiento
    ) {}

    /** DTO de respuesta para un Insumo, incluyendo stock total y lotes */
    public record InsumoResponseDto(
            Long insumoId,
            String nombre,
            String descripcion,
            String unidadMedida,
            BigDecimal stockMinimo,
            String proveedor,
            Boolean estado,
            BigDecimal cantidadStockTotal,
            List<LoteDto> lotes
    ) {}

    /** DTO para crear un nuevo Insumo junto con su primer lote */
    public record CreateInsumoRequestDto(
            @NotBlank String nombre,
            String descripcion,
            @NotBlank String unidadMedida,
            @NotNull @PositiveOrZero BigDecimal stockMinimo,
            String proveedor,

            // Datos del primer lote
            @NotNull @DecimalMin("0.01") BigDecimal cantidadInicial,
            @NotNull @DecimalMin("0.01") BigDecimal costoUnitario,
            LocalDate fechaVencimiento,
            String numeroLote
    ) {}

    /** DTO para actualizar los datos generales de un insumo (no el stock) */
    public record UpdateInsumoRequestDto(
            @NotBlank String nombre,
            String descripcion,
            @NotBlank String unidadMedida,
            @NotNull @PositiveOrZero BigDecimal stockMinimo,
            String proveedor,
            Boolean estado
    ) {}

    /** DTO para agregar un nuevo lote a un insumo existente */
    public record AddLoteRequestDto(
            @NotNull Long insumoId,
            @NotNull @DecimalMin("0.01") BigDecimal cantidad,
            @NotNull @DecimalMin("0.01") BigDecimal costoUnitario,
            LocalDate fechaVencimiento,
            String numeroLote
    ) {}
}