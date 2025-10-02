package com.gomitas.repository;

import com.gomitas.entity.Pronostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PronosticoRepository extends JpaRepository<Pronostico, Long> {
    List<Pronostico> findByProducto_ProductoIdAndFechaPronosticadaBetween(Long productoId, LocalDate fechaInicio, LocalDate fechaFin);
}
