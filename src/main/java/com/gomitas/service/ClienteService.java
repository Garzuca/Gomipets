package com.gomitas.service;

import com.gomitas.dto.ClienteDtos;
import com.gomitas.entity.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteService {

    List<ClienteDtos.ClienteResponseDto> getAllClientes();

    Optional<ClienteDtos.ClienteResponseDto> getClienteById(Long id);

    Optional<ClienteDtos.ClienteResponseDto> getClienteByUsuarioId(Long usuarioId);

    ClienteDtos.ClienteResponseDto createCliente(Cliente cliente);

    ClienteDtos.ClienteResponseDto updateCliente(Long id, ClienteDtos.ClienteUpdateDto clienteUpdate);

    void deleteCliente(Long id);

    List<ClienteDtos.ClienteResponseDto> buscarClientesPorNombre(String nombre);

    boolean existsByUsuarioId(Long usuarioId);
}

