package com.gomitas.service.impl;

import com.gomitas.dto.InsumoDtos;
import com.gomitas.entity.Insumo;
import com.gomitas.entity.LoteInsumo;
import com.gomitas.exception.ResourceNotFoundException;
import com.gomitas.repository.InsumoRepository;
import com.gomitas.repository.LoteInsumoRepository;
import com.gomitas.service.InsumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsumoServiceImpl implements InsumoService {

    private final InsumoRepository insumoRepository;
    private final LoteInsumoRepository loteInsumoRepository;

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
        // Crear la entidad principal Insumo
        Insumo insumo = Insumo.builder()
                .nombre(insumoDto.nombre())
                .descripcion(insumoDto.descripcion())
                .unidadMedida(insumoDto.unidadMedida())
                .stockMinimo(insumoDto.stockMinimo())
                .proveedor(insumoDto.proveedor())
                .estado(true)
                .build();
        Insumo savedInsumo = insumoRepository.save(insumo);

        // Crear el primer lote para este insumo
        LoteInsumo primerLote = LoteInsumo.builder()
                .insumo(savedInsumo)
                .cantidad(insumoDto.cantidadInicial())
                .costoUnitario(insumoDto.costoUnitario())
                .fechaIngreso(LocalDate.now())
                .fechaVencimiento(insumoDto.fechaVencimiento())
                .numeroLote(insumoDto.numeroLote())
                .build();
        loteInsumoRepository.save(primerLote);

        return getInsumoById(savedInsumo.getInsumoId());
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
        insumo.setProveedor(insumoDto.proveedor());
        insumo.setEstado(insumoDto.estado());

        insumoRepository.save(insumo);
        return mapToDto(insumo);
    }

    @Override
    @Transactional
    public InsumoDtos.LoteDto addLoteToInsumo(InsumoDtos.AddLoteRequestDto loteDto) {
        Insumo insumo = insumoRepository.findById(loteDto.insumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + loteDto.insumoId()));

        LoteInsumo nuevoLote = LoteInsumo.builder()
                .insumo(insumo)
                .cantidad(loteDto.cantidad())
                .costoUnitario(loteDto.costoUnitario())
                .fechaIngreso(LocalDate.now())
                .fechaVencimiento(loteDto.fechaVencimiento())
                .numeroLote(loteDto.numeroLote())
                .build();

        LoteInsumo savedLote = loteInsumoRepository.save(nuevoLote);
        return mapLoteToDto(savedLote);
    }

    private InsumoDtos.InsumoResponseDto mapToDto(Insumo insumo) {
        // Calcular el stock total sumando las cantidades de todos los lotes
        BigDecimal stockTotal = insumo.getLotes().stream()
                .map(LoteInsumo::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Mapear los lotes a sus DTOs
        List<InsumoDtos.LoteDto> loteDtos = insumo.getLotes().stream()
                .map(this::mapLoteToDto)
                .collect(Collectors.toList());

        return new InsumoDtos.InsumoResponseDto(
                insumo.getInsumoId(),
                insumo.getNombre(),
                insumo.getDescripcion(),
                insumo.getUnidadMedida(),
                insumo.getStockMinimo(),
                insumo.getProveedor(),
                insumo.getEstado(),
                stockTotal,
                loteDtos
        );
    }

    private InsumoDtos.LoteDto mapLoteToDto(LoteInsumo lote) {
        return new InsumoDtos.LoteDto(
                lote.getLoteId(),
                lote.getNumeroLote(),
                lote.getCantidad(),
                lote.getCostoUnitario(),
                lote.getFechaIngreso(),
                lote.getFechaVencimiento()
        );
    }
}