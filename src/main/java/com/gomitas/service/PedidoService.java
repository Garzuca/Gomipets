package com.gomitas.service;

import com.gomitas.dto.PedidoDtos;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface PedidoService {
    PedidoDtos.PedidoResponseDto createPedido(PedidoDtos.CreatePedidoRequestDto pedidoDto, Authentication authentication);
    List<PedidoDtos.PedidoResponseDto> findAll();
    Optional<PedidoDtos.PedidoResponseDto> findById(Long id);
    List<PedidoDtos.PedidoResponseDto> findByClienteId(Long clienteId);
    List<PedidoDtos.PedidoResponseDto> findByCurrentUser(Authentication authentication);
    PedidoDtos.PedidoResponseDto updatePedidoStatus(Long id, String estado);
    PedidoDtos.PedidoResponseDto despacharPedido(Long pedidoId);
}
