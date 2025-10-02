package com.gomitas.repository;

import com.gomitas.entity.Producto;
import com.gomitas.entity.ProductoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoInsumoRepository extends JpaRepository<ProductoInsumo, Long> {
    List<ProductoInsumo> findByProducto(Producto producto);
}