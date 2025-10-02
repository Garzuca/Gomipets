package com.gomitas.dto;

import com.gomitas.enums.MetodoPronostico;

import java.time.LocalDate;

public class PronosticoDtos {

    public record PronosticoRequestDto(
            Long productoId,
            MetodoPronostico metodo,
            LocalDate fechaFutura
            // Aquí se podrían añadir más parámetros específicos del método
    ) {}

    public record PronosticoResponseDto(
            Long pronosticoId,
            Long productoId,
            String nombreProducto,
            LocalDate fechaPronosticada,
            Integer cantidadEstimada,
            MetodoPronostico metodo,
            LocalDate fechaCalculo
    ) {}

    public record ErrorPronosticoDto(
            Long productoId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Double mad, // Desviación Absoluta Media
            Double mse, // Error Cuadrático Medio
            Double mape // Error Porcentual Absoluto Medio
    ) {}
}
