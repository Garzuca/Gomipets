package com.gomitas.repository;

import com.gomitas.entity.InventarioProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventarioProductoRepository extends JpaRepository<InventarioProducto, Long> {
    Optional<InventarioProducto> findByProducto_ProductoId(Long productoId);
}
