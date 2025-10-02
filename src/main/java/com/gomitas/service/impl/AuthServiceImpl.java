package com.gomitas.service.impl;

import com.gomitas.dto.AuthDtos;
import com.gomitas.entity.Cliente;
import com.gomitas.entity.Usuario;
import com.gomitas.enums.Rol;
import com.gomitas.exception.BadRequestException;
import com.gomitas.exception.ResourceNotFoundException;
import com.gomitas.repository.ClienteRepository;
import com.gomitas.repository.UsuarioRepository;
import com.gomitas.security.JwtUtils;
import com.gomitas.security.UserDetailsImpl;
import com.gomitas.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional(readOnly = true)
    public AuthDtos.AuthResponseDto login(AuthDtos.LoginRequestDto loginRequest) {
        log.debug("Iniciando proceso de login para usuario: {}", loginRequest.getNombreUsuario());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getNombreUsuario(),
                            loginRequest.getContraseña()
                    )
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(authentication);

            log.info("Login exitoso para usuario: {}", loginRequest.getNombreUsuario());

            return AuthDtos.AuthResponseDto.builder()
                    .token(jwt)
                    .refreshToken(refreshToken)
                    .usuarioId(userDetails.getUsuarioId())
                    .nombreUsuario(userDetails.getNombreUsuario())
                    .correo(userDetails.getCorreo())
                    .rol(userDetails.getRol())
                    .build();

        } catch (AuthenticationException e) {
            log.error("Error en autenticación para usuario: {}", loginRequest.getNombreUsuario());
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    @Override
    @Transactional
    public AuthDtos.AuthResponseDto register(AuthDtos.RegisterRequestDto registerRequest) {
        log.debug("Iniciando proceso de registro para usuario: {}", registerRequest.getNombreUsuario());

        if (usuarioRepository.existsByNombreUsuario(registerRequest.getNombreUsuario())) {
            throw new BadRequestException("Error: El nombre de usuario ya está en uso!");
        }

        if (registerRequest.getCorreo() != null && usuarioRepository.existsByCorreo(registerRequest.getCorreo())) {
            throw new BadRequestException("Error: El email ya está en uso!");
        }

        Usuario usuario = Usuario.builder()
                .nombreUsuario(registerRequest.getNombreUsuario())
                .correo(registerRequest.getCorreo())
                .password(passwordEncoder.encode(registerRequest.getContraseña()))
                .rol(Rol.CLIENTE)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.debug("Usuario creado con ID: {}", usuarioGuardado.getUsuarioId());

        Cliente cliente = Cliente.builder()
                .usuario(usuarioGuardado)
                .nombre(registerRequest.getNombre())
                .direccion(registerRequest.getDireccion())
                .telefono(registerRequest.getTelefono())
                .tipoMascota(registerRequest.getTipoMascota())
                .build();

        clienteRepository.save(cliente);
        log.debug("Cliente creado para usuario ID: {}", usuarioGuardado.getUsuarioId());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getNombreUsuario(),
                        registerRequest.getContraseña()
                )
        );

        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);

        log.info("Registro exitoso para usuario: {}", registerRequest.getNombreUsuario());

        return AuthDtos.AuthResponseDto.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .usuarioId(usuarioGuardado.getUsuarioId())
                .nombreUsuario(usuarioGuardado.getNombreUsuario())
                .correo(usuarioGuardado.getCorreo())
                .rol(usuarioGuardado.getRol())
                .fechaRegistro(usuarioGuardado.getFechaRegistro())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthDtos.UserInfoDto getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Usuario usuario = usuarioRepository.findById(userDetails.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return AuthDtos.UserInfoDto.builder()
                .usuarioId(usuario.getUsuarioId())
                .nombreUsuario(usuario.getNombreUsuario())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .fechaRegistro(usuario.getFechaRegistro())
                .estado(usuario.getEstado())
                .build();
    }

    @Override
    public AuthDtos.AuthResponseDto refreshToken(AuthDtos.RefreshTokenRequestDto refreshRequest) {
        String refreshToken = refreshRequest.refreshToken();

        if (!jwtUtils.validateJwtToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Refresh token inválido");
        }

        if (jwtUtils.isTokenExpired(refreshToken)) {
            throw new BadRequestException("Refresh token expirado");
        }

        String nombreUsuario = jwtUtils.getUserNameFromJwtToken(refreshToken);
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndEstadoTrue(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UserDetailsImpl userDetails = UserDetailsImpl.build(usuario);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        String newJwt = jwtUtils.generateJwtToken(newAuth);
        String newRefreshToken = jwtUtils.generateRefreshToken(newAuth);

        log.debug("Tokens renovados para usuario: {}", nombreUsuario);

        return AuthDtos.AuthResponseDto.builder()
                .token(newJwt)
                .refreshToken(newRefreshToken)
                .usuarioId(usuario.getUsuarioId())
                .nombreUsuario(usuario.getNombreUsuario())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNombreUsuario(String nombreUsuario) {
        return usuarioRepository.existsByNombreUsuario(nombreUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }
}