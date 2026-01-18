package com.gomitas.service;

import com.gomitas.dto.InsumoDtos;

import java.util.List;

public interface InsumoService {
    List<InsumoDtos.InsumoResponseDto> getAllInsumos();
    InsumoDtos.InsumoResponseDto getInsumoById(Long id);
    InsumoDtos.InsumoResponseDto createInsumo(InsumoDtos.CreateInsumoRequestDto insumoDto);
    InsumoDtos.InsumoResponseDto updateInsumo(Long id, InsumoDtos.UpdateInsumoRequestDto insumoDto);
    InsumoDtos.LoteDto addLoteToInsumo(InsumoDtos.AddLoteRequestDto loteDto);
    // Podríamos añadir más métodos específicos para lotes si es necesario,
    // como getLotesByInsumoId(Long insumoId), etc.
}