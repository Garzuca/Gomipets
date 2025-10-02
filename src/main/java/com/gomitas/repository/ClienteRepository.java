package com.gomitas.repository;

import com.gomitas.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("SELECT c FROM Cliente c JOIN FETCH c.usuario WHERE c.usuario.usuarioId = :usuarioId")
    Optional<Cliente> findByUsuarioIdWithUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT c FROM Cliente c JOIN FETCH c.usuario")
    List<Cliente> findAllWithUsuario();

    @Query("SELECT c FROM Cliente c JOIN FETCH c.usuario WHERE c.clienteId = :id")
    Optional<Cliente> findByIdWithUsuario(@Param("id") Long id);

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    boolean existsByUsuario_UsuarioId(Long usuarioId);
}