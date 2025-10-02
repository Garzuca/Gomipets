package com.gomitas.service.impl;

import com.gomitas.dto.ClienteDtos;
import com.gomitas.entity.Cliente;
import com.gomitas.entity.Usuario;
import com.gomitas.exception.ResourceNotFoundException;
import com.gomitas.repository.ClienteRepository;
import com.gomitas.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDtos.ClienteResponseDto> getAllClientes() {
        return clienteRepository.findAllWithUsuario().stream()
                .map(this::mapToClienteResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDtos.ClienteResponseDto> getClienteById(Long id) {
        return clienteRepository.findByIdWithUsuario(id)
                .map(this::mapToClienteResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDtos.ClienteResponseDto> getClienteByUsuarioId(Long usuarioId) {
        return clienteRepository.findByUsuarioIdWithUsuario(usuarioId)
                .map(this::mapToClienteResponseDto);
    }

    @Override
    @Transactional
    public ClienteDtos.ClienteResponseDto createCliente(Cliente cliente) {
        Cliente savedCliente = clienteRepository.save(cliente);
        return mapToClienteResponseDto(savedCliente);
    }

    @Override
    @Transactional
    public ClienteDtos.ClienteResponseDto updateCliente(Long id, ClienteDtos.ClienteUpdateDto clienteUpdate) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        cliente.setNombre(clienteUpdate.nombre());
        cliente.setDireccion(clienteUpdate.direccion());
        cliente.setTelefono(clienteUpdate.telefono());
        cliente.setTipoMascota(clienteUpdate.tipoMascota());

        Cliente updatedCliente = clienteRepository.save(cliente);
        return mapToClienteResponseDto(updatedCliente);
    }

    @Override
    @Transactional
    public void deleteCliente(Long id) {
        Cliente cliente = clienteRepository.findByIdWithUsuario(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        Usuario usuario = cliente.getUsuario();
        if (usuario != null) {
            usuario.setEstado(false); // Desactivar el usuario asociado
            // El repositorio de usuarios guardará el cambio por la cascada de persistencia o explícitamente si es necesario.
        }
        // No se borra el cliente para mantener la integridad de los pedidos históricos
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDtos.ClienteResponseDto> buscarClientesPorNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::mapToClienteResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsuarioId(Long usuarioId) {
        return clienteRepository.existsByUsuario_UsuarioId(usuarioId);
    }

    private ClienteDtos.ClienteResponseDto mapToClienteResponseDto(Cliente cliente) {
        Usuario usuario = cliente.getUsuario();
        return new ClienteDtos.ClienteResponseDto(
                cliente.getClienteId(),
                usuario.getUsuarioId(),
                usuario.getNombreUsuario(),
                usuario.getCorreo(),
                cliente.getNombre(),
                cliente.getDireccion(),
                cliente.getTelefono(),
                cliente.getTipoMascota()
        );
    }
}