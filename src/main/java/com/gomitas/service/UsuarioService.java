package com.gomitas.service;

import com.gomitas.dto.AuthDtos;
import com.gomitas.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    List<AuthDtos.UserInfoDto> getAllUsuarios();

    Optional<AuthDtos.UserInfoDto> getUsuarioById(Long id);

    Optional<AuthDtos.UserInfoDto> getUsuarioByNombreUsuario(String nombreUsuario);

    AuthDtos.UserInfoDto createUsuario(Usuario usuario);

    AuthDtos.UserInfoDto updateUsuario(Long id, Usuario usuario);

    void deleteUsuario(Long id);

    void deactivateUsuario(Long id);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByCorreo(String correo);
}
