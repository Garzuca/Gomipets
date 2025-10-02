package com.gomitas.repository;

import com.gomitas.entity.ParametrosEoq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametrosEoqRepository extends JpaRepository<ParametrosEoq, Long> {
}
