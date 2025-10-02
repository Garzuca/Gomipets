package com.gomitas.service.impl;

import com.gomitas.dto.InsumoDtos;
import com.gomitas.entity.Insumo;
import com.gomitas.exception.ResourceNotFoundException;
import com.gomitas.repository.InsumoRepository;
import com.gomitas.service.InsumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsumoServiceImpl implements InsumoService {

    private final InsumoRepository insumoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InsumoDtos.InsumoResponseDto> getAllInsumos() {
        return insumoRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InsumoDtos.InsumoResponseDto getInsumoById(Long id) {
        return insumoRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public InsumoDtos.InsumoResponseDto createInsumo(InsumoDtos.CreateInsumoRequestDto insumoDto) {
        Insumo insumo = Insumo.builder()
                .nombre(insumoDto.nombre())
                .descripcion(insumoDto.descripcion())
                .unidadMedida(insumoDto.unidadMedida())
                .cantidadStock(insumoDto.cantidadStock())
                .stockMinimo(insumoDto.stockMinimo())
                .fechaIngreso(LocalDate.now())
                .fechaVencimiento(insumoDto.fechaVencimiento())
                .costoUnitario(insumoDto.costoUnitario())
                .proveedor(insumoDto.proveedor())
                .estado(true)
                .build();
        Insumo savedInsumo = insumoRepository.save(insumo);
        return mapToDto(savedInsumo);
    }

    @Override
    @Transactional
    public InsumoDtos.InsumoResponseDto updateInsumo(Long id, InsumoDtos.UpdateInsumoRequestDto insumoDto) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + id));

        insumo.setNombre(insumoDto.nombre());
        insumo.setDescripcion(insumoDto.descripcion());
        insumo.setUnidadMedida(insumoDto.unidadMedida());
        insumo.setStockMinimo(insumoDto.stockMinimo());
        insumo.setFechaVencimiento(insumoDto.fechaVencimiento());
        insumo.setCostoUnitario(insumoDto.costoUnitario());
        insumo.setProveedor(insumoDto.proveedor());
        insumo.setEstado(insumoDto.estado());

        Insumo updatedInsumo = insumoRepository.save(insumo);
        return mapToDto(updatedInsumo);
    }

    private InsumoDtos.InsumoResponseDto mapToDto(Insumo insumo) {
        return new InsumoDtos.InsumoResponseDto(
                insumo.getInsumoId(),
                insumo.getNombre(),
                insumo.getDescripcion(),
                insumo.getUnidadMedida(),
                insumo.getCantidadStock(),
                insumo.getStockMinimo(),
                insumo.getFechaIngreso(),
                insumo.getFechaVencimiento(),
                insumo.getCostoUnitario(),
                insumo.getProveedor(),
                insumo.getEstado()
        );
    }
}
