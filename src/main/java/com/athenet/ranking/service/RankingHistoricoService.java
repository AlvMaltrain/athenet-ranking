package com.athenet.ranking.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.athenet.ranking.dto.response.RankingHistoricoResponse;
import com.athenet.ranking.dto.response.UltimoEncuentroResponse;
import com.athenet.ranking.mapper.RankingHistoricoMapper;
import com.athenet.ranking.model.RankingHistorico;
import com.athenet.ranking.model.ResultadoProcesado;
import com.athenet.ranking.repository.RankingHistoricoRepository;
import com.athenet.ranking.repository.ResultadoProcesadoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Mantiene el ranking histórico de un deporte: recalcula las estadísticas
 * de los dos equipos involucrados en un resultado y reordena el ranking
 * completo del deporte (cruza competencias, a diferencia de la tabla de
 * posiciones que es por competencia).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RankingHistoricoService {

    private final ResultadoProcesadoRepository resultadoProcesadoRepository;
    private final RankingHistoricoRepository rankingHistoricoRepository;
    private final RankingHistoricoMapper rankingHistoricoMapper;

    /**
     * Devuelve el ranking histórico de un deporte, ya ordenado, con el
     * ultimo encuentro de cada equipo.
     */
    @Transactional(readOnly = true)
    public List<RankingHistoricoResponse> obtenerRanking(Long deporteId) {
        return rankingHistoricoRepository.findByDeporteIdOrderByPosicionAsc(deporteId).stream()
                .map(fila -> rankingHistoricoMapper.toResponse(fila, obtenerUltimoEncuentro(deporteId, fila.getEquipoId())))
                .toList();
    }

    // Mas reciente primero (ver ResultadoProcesadoRepository); null si el equipo no tiene
    // ningun resultado procesado todavia en este deporte.
    private UltimoEncuentroResponse obtenerUltimoEncuentro(Long deporteId, Long equipoId) {
        return resultadoProcesadoRepository.findByDeporteIdAndEquipoOrdenadoPorFechaDesc(deporteId, equipoId).stream()
                .findFirst()
                .map(this::toUltimoEncuentroResponse)
                .orElse(null);
    }

    private UltimoEncuentroResponse toUltimoEncuentroResponse(ResultadoProcesado r) {
        return new UltimoEncuentroResponse(
                r.getCompetenciaId(),
                r.getEquipoLocalId(),
                r.getInstitucionLocalId(),
                r.getEquipoVisitaId(),
                r.getInstitucionVisitaId(),
                r.getPuntajeLocal(),
                r.getPuntajeVisita(),
                r.getFechaPartido()
        );
    }

    public void actualizar(ResultadoProcesado resultado) {
        Long deporteId = resultado.getDeporteId();

        recalcularEquipo(deporteId, resultado.getEquipoLocalId(), resultado.getInstitucionLocalId());
        recalcularEquipo(deporteId, resultado.getEquipoVisitaId(), resultado.getInstitucionVisitaId());
        reordenar(deporteId);
    }

    // Recalcula desde cero las estadísticas históricas de un equipo en el deporte (evita desincronización por conteos incrementales)
    private void recalcularEquipo(Long deporteId, Long equipoId, Long institucionId) {
        List<ResultadoProcesado> resultados = resultadoProcesadoRepository.findByDeporteIdAndEquipo(deporteId, equipoId);

        int pj = 0, pg = 0, pe = 0, pp = 0, puntosFavor = 0, puntosContra = 0, pts = 0;
        for (ResultadoProcesado r : resultados) {
            boolean esLocal = equipoId.equals(r.getEquipoLocalId());
            int puntajePropio = esLocal ? r.getPuntajeLocal() : r.getPuntajeVisita();
            int puntajeRival = esLocal ? r.getPuntajeVisita() : r.getPuntajeLocal();

            pj++;
            puntosFavor += puntajePropio;
            puntosContra += puntajeRival;
            if (puntajePropio > puntajeRival) {
                pg++;
                pts += r.getPuntosVictoria();
            } else if (puntajePropio == puntajeRival) {
                pe++;
                pts += r.getPuntosEmpate();
            } else {
                pp++;
                pts += r.getPuntosDerrota();
            }
        }

        RankingHistorico fila = rankingHistoricoRepository.findByDeporteIdAndEquipoId(deporteId, equipoId)
                .orElseGet(RankingHistorico::new);
        fila.setDeporteId(deporteId);
        fila.setEquipoId(equipoId);
        fila.setInstitucionId(institucionId);
        fila.setPj(pj);
        fila.setPg(pg);
        fila.setPe(pe);
        fila.setPp(pp);
        fila.setPuntosFavor(puntosFavor);
        fila.setPuntosContra(puntosContra);
        fila.setDiferencia(puntosFavor - puntosContra);
        fila.setPts(pts);
        rankingHistoricoRepository.save(fila);
    }

    // Reasigna la posición de todos los equipos del deporte (se ve afectado por cada resultado nuevo, no solo los dos equipos que jugaron)
    private void reordenar(Long deporteId) {
        List<RankingHistorico> filas = rankingHistoricoRepository.findByDeporteId(deporteId).stream()
                .sorted(Comparator.comparing(RankingHistorico::getPts).reversed()
                        .thenComparing(Comparator.comparing(RankingHistorico::getDiferencia).reversed())
                        .thenComparing(Comparator.comparing(RankingHistorico::getPuntosFavor).reversed()))
                .toList();

        for (int i = 0; i < filas.size(); i++) {
            filas.get(i).setPosicion(i + 1);
        }

        rankingHistoricoRepository.saveAll(filas);
    }
}
