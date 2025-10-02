package com.gomitas.repository;

import com.gomitas.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
    @Query("SELECT dp FROM DetallePedido dp " +
           "JOIN dp.pedido p " +
           "WHERE (:fechaInicio IS NULL OR p.fechaPedido >= :fechaInicio) " +
           "AND (:fechaFin IS NULL OR p.fechaPedido <= :fechaFin) " +
           "AND (:productoId IS NULL OR dp.producto.productoId = :productoId) " +
           "AND (:clienteId IS NULL OR p.cliente.clienteId = :clienteId)")
    List<DetallePedido> findVentasByCriteria(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("productoId") Long productoId,
            @Param("clienteId") Long clienteId
    );
}