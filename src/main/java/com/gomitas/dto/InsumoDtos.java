package com.gomitas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InsumoDtos {

    public record InsumoResponseDto(
            Long insumoId,
            String nombre,
            String descripcion,
            String unidadMedida,
            BigDecimal cantidadStock,
            BigDecimal stockMinimo,
            LocalDate fechaIngreso,
            LocalDate fechaVencimiento,
            BigDecimal costoUnitario,
            String proveedor,
            Boolean estado
    ) {}

    public record CreateInsumoRequestDto(
            String nombre,
            String descripcion,
            String unidadMedida,
            BigDecimal cantidadStock,
            BigDecimal stockMinimo,
            LocalDate fechaVencimiento,
            BigDecimal costoUnitario,
            String proveedor
    ) {}

    public record UpdateInsumoRequestDto(
            String nombre,
            String descripcion,
            String unidadMedida,
            BigDecimal stockMinimo,
            LocalDate fechaVencimiento,
            BigDecimal costoUnitario,
            String proveedor,
            Boolean estado
    ) {}
}
