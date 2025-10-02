package com.gomitas.entity;

import com.gomitas.enums.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long usuarioId;

    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Column(name = "nombre_usuario", unique = true, nullable = false)
    private String nombreUsuario;

    @Email(message = "Debe ser un email válido")
    @Column(name = "correo", unique = true)
    private String correo;

    @NotBlank(message = "La contraseña es requerida")
    @Column(name = "password", nullable = false)
    private String password;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @NotNull(message = "El rol es requerido")
    @Column(name = "rol", nullable = false)
    private Rol rol;

    @Builder.Default
    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro = LocalDate.now();

    @Builder.Default
    @Column(name = "estado")
    private Boolean estado = true;
}
