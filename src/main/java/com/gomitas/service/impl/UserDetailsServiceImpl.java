package com.gomitas.service.impl;

import com.gomitas.entity.Usuario;
import com.gomitas.repository.UsuarioRepository;
import com.gomitas.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("cargando usarios por username: {}",username);

        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}",username);
                    return new UsernameNotFoundException("Usuarios no encontrado");
                });
        log.debug("usuario encontrado: {} con rool: {}",usuario.getNombreUsuario(), usuario.getRol());
        return UserDetailsImpl.build(usuario);
    }
}
