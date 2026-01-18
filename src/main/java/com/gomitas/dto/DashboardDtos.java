package com.gomitas.dto;

import java.util.List;

public class DashboardDtos {

    // DTO para un indicador clave (KPI) individual
    public record KpiDto(
            Object value,
            String label
    ) {}

    // DTO para un conjunto de datos dentro de un gráfico (ej. una línea en un gráfico de líneas)
    public record DatasetDto(
            String label,
            List<? extends Number> data, // Acepta listas de Integer, Long, Double, etc.
            List<String> backgroundColor,
            String borderColor,
            double tension
    ) {
        // Constructor simplificado para gráficos de líneas
        public DatasetDto(String label, List<? extends Number> data) {
            this(label, data, null, "rgb(75, 192, 192)", 0.1);
        }

        // Constructor simplificado para gráficos de anillo/barra con colores
        public DatasetDto(String label, List<? extends Number> data, List<String> backgroundColors) {
            this(label, data, backgroundColors, null, 0);
        }
    }

    // DTO para los datos completos de un gráfico
    public record ChartDataDto(
            List<String> labels,
            List<DatasetDto> datasets
    ) {}

    // DTO principal que se enviará como respuesta de la API
    public record MetricasResponseDto(
            List<KpiDto> kpis,
            ChartDataDto salesOverTime,
            ChartDataDto topProducts
    ) {}
}
