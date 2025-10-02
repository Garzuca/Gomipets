package com.gomitas.security;

import com.gomitas.entity.Usuario;
import com.gomitas.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementación separada de UserDetails para manejar la autenticación
 * sin mezclar responsabilidades con la entidad Usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Long usuarioId;
    private String nombreUsuario;
    private String correo;
    private String contraseña;
    private Rol rol;
    private Boolean estado;

    /**
     * Factory method para crear UserDetailsImpl desde Usuario
     */
    public static UserDetailsImpl build(Usuario usuario) {
        return UserDetailsImpl.builder()
                .usuarioId(usuario.getUsuarioId())
                .nombreUsuario(usuario.getNombreUsuario())
                .correo(usuario.getCorreo())
                .contraseña(usuario.getPassword())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_"+rol.name())
        );
    }

    @Override
    public String getPassword() {
        return contraseña;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return estado != null && estado;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return estado != null && estado;
    }
}
