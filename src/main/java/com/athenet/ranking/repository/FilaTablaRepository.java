package com.athenet.ranking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.athenet.ranking.model.FilaTabla;

public interface FilaTablaRepository extends JpaRepository<FilaTabla, Long> {

    Optional<FilaTabla> findByCompetenciaIdAndEquipoId(Long competenciaId, Long equipoId); // Fila existente de un equipo en una competencia (para actualizar en vez de duplicar)

    List<FilaTabla> findByCompetenciaIdOrderByPosicionAsc(Long competenciaId); // Tabla de posiciones completa de una competencia, ya ordenada
}
