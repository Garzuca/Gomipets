package com.gomitas.entity;

import com.gomitas.enums.PrioridadAlerta;
import com.gomitas.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alerta_sistema")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alerta_id")
    private Long alertaId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_alerta")
    private TipoAlerta tipo;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(name = "entidad_tipo")
    private String entidadTipo;

    @Column(nullable = false)
    private String mensaje;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "prioridad_alerta")
    private PrioridadAlerta prioridad;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Builder.Default
    private boolean leida = false;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;
}
