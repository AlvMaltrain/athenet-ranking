package com.athenet.ranking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.athenet.ranking.model.ResultadoProcesado;

public interface ResultadoProcesadoRepository extends JpaRepository<ResultadoProcesado, Long> {

    List<ResultadoProcesado> findByCompetenciaId(Long competenciaId); // Resultados de una competencia (para recalcular su tabla)

    // Resultados históricos de un equipo en un deporte, sin importar la competencia (para el ranking histórico)
    @Query("SELECT r FROM ResultadoProcesado r WHERE r.deporteId = :deporteId "
            + "AND (r.equipoLocalId = :equipoId OR r.equipoVisitaId = :equipoId)")
    List<ResultadoProcesado> findByDeporteIdAndEquipo(@Param("deporteId") Long deporteId, @Param("equipoId") Long equipoId);

    // Mismo filtro que el de arriba, pero ordenado del mas reciente al mas antiguo (para
    // el "ultimo encuentro" en el ranking historico). fechaPartido puede ser null en filas
    // viejas o mientras Competencia no la envie; NULLS LAST evita que esas tapen un partido
    // mas reciente que si tenga fecha, y partidoId desc queda como desempate razonable.
    @Query("SELECT r FROM ResultadoProcesado r WHERE r.deporteId = :deporteId "
            + "AND (r.equipoLocalId = :equipoId OR r.equipoVisitaId = :equipoId) "
            + "ORDER BY r.fechaPartido DESC NULLS LAST, r.partidoId DESC")
    List<ResultadoProcesado> findByDeporteIdAndEquipoOrdenadoPorFechaDesc(@Param("deporteId") Long deporteId, @Param("equipoId") Long equipoId);
}
