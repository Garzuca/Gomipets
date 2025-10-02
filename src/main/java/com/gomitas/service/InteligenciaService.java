package com.gomitas.service;

import com.gomitas.dto.AlertaDtos;
import com.gomitas.dto.AnalisisDtos;
import com.gomitas.dto.PronosticoDtos;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.List;

public interface InteligenciaService {

    PronosticoDtos.PronosticoResponseDto generarPronostico(PronosticoDtos.PronosticoRequestDto requestDto);

    PronosticoDtos.ErrorPronosticoDto calcularErrorDePronostico(Long productoId, LocalDate fechaInicio, LocalDate fechaFin);

    AnalisisDtos.EoqResponseDto calcularEoq(AnalisisDtos.EoqRequestDto requestDto);

    List<AnalisisDtos.AbcResponseDto> generarClasificacionAbc(LocalDate fechaInicio, LocalDate fechaFin);

    List<AlertaDtos.AlertaResponseDto> getAlertasActivas();

    AlertaDtos.AlertaResponseDto marcarAlertaComoLeida(Long alertaId);
}
