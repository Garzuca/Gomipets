package com.gomitas.repository;

import com.gomitas.entity.InventarioProducto;
import com.gomitas.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioProductoRepository extends JpaRepository<InventarioProducto, Long> {
    // Método corregido: busca por la entidad Producto directamente.
    Optional<InventarioProducto> findByProducto(Producto producto);

    // Este método es necesario para la tarea programada y es correcto.
    @Query("SELECT i FROM InventarioProducto i JOIN FETCH i.producto")
    List<InventarioProducto> findAllWithProducto();
}
