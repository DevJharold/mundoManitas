package com.mundomanitas.repository;

import com.mundomanitas.entity.Progreso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgresoRepository extends JpaRepository<Progreso, Integer> {

    List<Progreso> findByIdEstudiante(Integer idEstudiante);

    Optional<Progreso> findByIdEstudianteAndIdModulo(
        Integer idEstudiante,
        Integer idModulo
    );
}