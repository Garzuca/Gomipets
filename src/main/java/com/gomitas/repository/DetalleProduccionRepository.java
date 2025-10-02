package com.gomitas.repository;

import com.gomitas.entity.DetalleProduccion;
import com.gomitas.entity.OrdenProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleProduccionRepository extends JpaRepository<DetalleProduccion, Long> {
    List<DetalleProduccion> findByOrdenProduccion(OrdenProduccion ordenProduccion);
}