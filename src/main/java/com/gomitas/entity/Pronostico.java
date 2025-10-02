package com.gomitas.entity;

import com.gomitas.enums.MetodoPronostico;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pronostico")
public class Pronostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "fecha_pronosticada", nullable = false)
    private LocalDate fechaPronosticada;

    @Column(name = "cantidad_estimada", nullable = false)
    private Integer cantidadEstimada;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "metodo")
    private MetodoPronostico metodo;

    @CreationTimestamp
    @Column(name = "fecha_calculo")
    private LocalDateTime fechaCalculo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
