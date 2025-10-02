package com.gomitas.service;

import com.gomitas.dto.ProduccionDtos;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProduccionService {
    List<ProduccionDtos.OrdenProduccionResponseDto> getAllOrdenes();
    ProduccionDtos.OrdenProduccionResponseDto getOrdenById(Long id);
    ProduccionDtos.OrdenProduccionResponseDto createOrdenProduccion(ProduccionDtos.CreateOrdenProduccionRequestDto requestDto, Authentication authentication);
    ProduccionDtos.OrdenProduccionResponseDto iniciarOrden(Long id, Authentication authentication);
    ProduccionDtos.OrdenProduccionResponseDto completarOrden(Long id, Authentication authentication);
    ProduccionDtos.OrdenProduccionResponseDto cancelarOrden(Long id, Authentication authentication);
    ProduccionDtos.RecetaProductoResponseDto getRecetaByProductoId(Long productoId);
}