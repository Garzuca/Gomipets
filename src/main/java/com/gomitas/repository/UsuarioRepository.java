package com.gomitas.repository;

import com.gomitas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByCorreo(String correo);

    @Query("SELECT u FROM Usuario u WHERE u.nombreUsuario = :nombreUsuario AND u.estado = true")
    Optional<Usuario> findByNombreUsuarioAndEstadoTrue(@Param("nombreUsuario") String nombreUsuario);

    Boolean existsByNombreUsuario(String nombreUsuario);

    Boolean existsByCorreo(String correo);

    @Query("SELECT u FROM Usuario u WHERE u.estado = true")
    java.util.List<Usuario> findAllActive();
}
