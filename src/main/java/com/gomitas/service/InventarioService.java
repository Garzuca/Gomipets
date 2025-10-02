package com.gomitas.service;

import com.gomitas.dto.InventarioDtos;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface InventarioService {
    List<InventarioDtos.InventarioResponseDto> getEstadoInventario();
    InventarioDtos.InventarioResponseDto getInventarioPorProductoId(Long productoId);
    InventarioDtos.MovimientoResponseDto registrarMovimiento(InventarioDtos.MovimientoRequestDto movimientoDto, Authentication authentication);
    List<InventarioDtos.InventarioResponseDto> getProductosConStockBajo();
    boolean hayStockSuficiente(Long productoId, Integer cantidadRequerida);
}