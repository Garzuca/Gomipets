package com.gomitas.service;

import com.gomitas.dto.ProductoDtos;

import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<ProductoDtos.ProductoResponseDto> findAll();
    List<ProductoDtos.ProductoResponseDto> findAllActive();
    Optional<ProductoDtos.ProductoResponseDto> findById(Long id);
    ProductoDtos.ProductoResponseDto save(ProductoDtos.CreateProductoRequestDto productoDto);
    ProductoDtos.ProductoResponseDto update(Long id, ProductoDtos.UpdateProductoRequestDto productoDto);
    void deleteById(Long id);
}
