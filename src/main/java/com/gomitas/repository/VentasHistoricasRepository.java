package com.gomitas.repository;

import com.gomitas.entity.VentasHistoricas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VentasHistoricasRepository extends JpaRepository<VentasHistoricas, Long> {
    List<VentasHistoricas> findByProducto_ProductoIdAndFechaVentaBetween(Long productoId, LocalDate fechaInicio, LocalDate fechaFin);
    List<VentasHistoricas> findByFechaVentaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    // Método para filtrar por producto y ordenar por fecha de forma eficiente
    List<VentasHistoricas> findByProducto_ProductoIdOrderByFechaVentaDesc(Long productoId);
}
