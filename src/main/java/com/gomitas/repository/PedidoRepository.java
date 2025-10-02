package com.gomitas.repository;

import com.gomitas.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p " +
           "JOIN FETCH p.cliente c " +
           "JOIN FETCH c.usuario " +
           "JOIN FETCH p.detalles d " +
           "JOIN FETCH d.producto " +
           "WHERE p.pedidoId = :id")
    Optional<Pedido> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Pedido p " +
           "JOIN FETCH p.cliente c " +
           "JOIN FETCH c.usuario " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto")
    List<Pedido> findAllWithDetails();

    @Query("SELECT DISTINCT p FROM Pedido p " +
           "JOIN FETCH p.cliente c " +
           "JOIN FETCH c.usuario " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE c.clienteId = :clienteId")
    List<Pedido> findByCliente_ClienteIdWithDetails(@Param("clienteId") Long clienteId);

    @Query("SELECT DISTINCT p FROM Pedido p " +
           "JOIN FETCH p.cliente c " +
           "JOIN FETCH c.usuario u " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE u.usuarioId = :usuarioId")
    List<Pedido> findByCliente_Usuario_UsuarioIdWithDetails(@Param("usuarioId") Long usuarioId);
}