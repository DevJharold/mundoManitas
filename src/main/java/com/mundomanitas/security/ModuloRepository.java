package com.mundomanitas.repository;

import com.mundomanitas.entity.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

    List<Modulo> findAllByOrderByOrdenAsc();
}