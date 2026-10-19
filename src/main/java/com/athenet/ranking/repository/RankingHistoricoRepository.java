package com.athenet.ranking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.athenet.ranking.model.RankingHistorico;

public interface RankingHistoricoRepository extends JpaRepository<RankingHistorico, Long> {

    Optional<RankingHistorico> findByDeporteIdAndEquipoId(Long deporteId, Long equipoId); // Fila existente de un equipo en un deporte (para actualizar en vez de duplicar)

    List<RankingHistorico> findByDeporteId(Long deporteId); // Todas las filas de un deporte (para reordenar)

    List<RankingHistorico> findByDeporteIdOrderByPosicionAsc(Long deporteId); // Ranking histórico completo de un deporte, ya ordenado
}
