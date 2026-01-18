package com.gomitas.repository;

import com.gomitas.entity.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    @Query("SELECT i FROM Insumo i LEFT JOIN FETCH i.lotes")
    List<Insumo> findAllWithLotes();
}