package com.gomitas.repository;

import com.gomitas.entity.ClasificacionAbc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClasificacionAbcRepository extends JpaRepository<ClasificacionAbc, Long> {
}
