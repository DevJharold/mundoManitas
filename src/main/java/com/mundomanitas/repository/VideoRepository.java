package com.mundomanitas.repository;

import com.mundomanitas.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Integer> {

    List<Video> findByIdModuloOrderByOrdenAsc(Integer idModulo);
}