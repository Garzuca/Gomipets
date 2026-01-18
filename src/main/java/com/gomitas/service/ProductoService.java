package com.gomitas.service;

import com.gomitas.dto.ProductoDtos;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<ProductoDtos.ProductoResponseDto> findAll();
    List<ProductoDtos.ProductoResponseDto> findAllActive();
    Optional<ProductoDtos.ProductoResponseDto> findById(Long id);
    ProductoDtos.ProductoResponseDto save(ProductoDtos.CreateProductoRequestDto productoDto, MultipartFile imagen) throws IOException;
    ProductoDtos.ProductoResponseDto update(Long id, ProductoDtos.UpdateProductoRequestDto productoDto, MultipartFile imagen) throws IOException;
    void deleteById(Long id);

    List<ProductoDtos.RecetaResponseDto> getRecetaByProductoId(Long productoId);

    List<ProductoDtos.RecetaResponseDto> addRecetaToProducto(Long productoId, ProductoDtos.CreateRecetaRequestDto recetaDto);
}
