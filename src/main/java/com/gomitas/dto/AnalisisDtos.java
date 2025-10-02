package com.gomitas.dto;

import com.gomitas.enums.CategoriaABC;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AnalisisDtos {

    public record EoqRequestDto(
            Long productoId,
            Integer demandaAnual,
            BigDecimal costoPedido,
            BigDecimal costoMantenimiento
    ) {}

    public record EoqResponseDto(
            Long productoId,
            BigDecimal eoqCalculado,
            Integer puntoReorden,
            LocalDateTime fechaCalculo
    ) {}

    public record AbcResponseDto(
            Long productoId,
            String nombreProducto,
            BigDecimal valorVentasAnuales,
            CategoriaABC categoria
    ) {}
}
