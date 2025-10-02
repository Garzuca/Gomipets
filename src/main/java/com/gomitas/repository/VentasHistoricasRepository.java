package com.gomitas.repository;

import com.gomitas.entity.VentasHistoricas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VentasHistoricasRepository extends JpaRepository<VentasHistoricas, Long> {
    List<VentasHistoricas> findByProducto_ProductoIdAndFechaVentaBetween(Long productoId, LocalDate fechaInicio, LocalDate fechaFin);
    List<VentasHistoricas> findByFechaVentaBetween(LocalDate fechaInicio, LocalDate fechaFin);
}
