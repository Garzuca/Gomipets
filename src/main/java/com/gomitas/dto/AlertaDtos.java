package com.gomitas.dto;

import com.gomitas.enums.PrioridadAlerta;
import com.gomitas.enums.TipoAlerta;

import java.time.LocalDateTime;

public class AlertaDtos {

    public record AlertaResponseDto(
            Long alertaId,
            TipoAlerta tipo,
            String mensaje,
            PrioridadAlerta prioridad,
            LocalDateTime fechaCreacion,
            boolean leida
    ) {}
}
