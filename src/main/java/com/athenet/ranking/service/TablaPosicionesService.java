package com.athenet.ranking.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.athenet.ranking.dto.response.FilaTablaResponse;
import com.athenet.ranking.mapper.FilaTablaMapper;
import com.athenet.ranking.model.FilaTabla;
import com.athenet.ranking.model.ResultadoProcesado;
import com.athenet.ranking.repository.FilaTablaRepository;
import com.athenet.ranking.repository.ResultadoProcesadoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Recalcula la tabla de posiciones de una competencia desde cero, a partir
 * de todos sus resultados procesados (evita desincronización por conteos
 * incrementales).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TablaPosicionesService {

    private final ResultadoProcesadoRepository resultadoProcesadoRepository;
    private final FilaTablaRepository filaTablaRepository;
    private final FilaTablaMapper filaTablaMapper;

    /**
     * Devuelve la tabla de posiciones de una competencia, ya ordenada.
     */
    @Transactional(readOnly = true)
    public List<FilaTablaResponse> obtenerTabla(Long competenciaId) {
        return filaTablaRepository.findByCompetenciaIdOrderByPosicionAsc(competenciaId).stream()
                .map(filaTablaMapper::toResponse)
                .toList();
    }

    public void recalcular(Long competenciaId) {
        List<ResultadoProcesado> resultados = resultadoProcesadoRepository.findByCompetenciaId(competenciaId);

        Map<Long, EstadisticasEquipo> estadisticas = new LinkedHashMap<>();
        for (ResultadoProcesado r : resultados) {
            acumular(estadisticas, r.getEquipoLocalId(), r.getInstitucionLocalId(),
                    r.getPuntajeLocal(), r.getPuntajeVisita(), r);
            acumular(estadisticas, r.getEquipoVisitaId(), r.getInstitucionVisitaId(),
                    r.getPuntajeVisita(), r.getPuntajeLocal(), r);
        }

        List<FilaTabla> filas = estadisticas.values().stream()
                .map(e -> toFilaTabla(competenciaId, e))
                .sorted(Comparator.comparing(FilaTabla::getPts).reversed()
                        .thenComparing(Comparator.comparing(FilaTabla::getDiferencia).reversed())
                        .thenComparing(Comparator.comparing(FilaTabla::getPuntosFavor).reversed()))
                .toList();

        for (int i = 0; i < filas.size(); i++) {
            filas.get(i).setPosicion(i + 1);
        }

        filaTablaRepository.saveAll(filas);
    }

    // Acumula pj/pg/pe/pp, puntosFavor/puntosContra y pts de un equipo a partir de un resultado
    private void acumular(Map<Long, EstadisticasEquipo> acumulado, Long equipoId, Long institucionId,
                           int puntajePropio, int puntajeRival, ResultadoProcesado r) {
        EstadisticasEquipo e = acumulado.computeIfAbsent(equipoId, id -> new EstadisticasEquipo(id, institucionId));
        e.pj++;
        e.puntosFavor += puntajePropio;
        e.puntosContra += puntajeRival;
        if (puntajePropio > puntajeRival) {
            e.pg++;
            e.pts += r.getPuntosVictoria();
        } else if (puntajePropio == puntajeRival) {
            e.pe++;
            e.pts += r.getPuntosEmpate();
        } else {
            e.pp++;
            e.pts += r.getPuntosDerrota();
        }
    }

    // Vuelca las estadísticas acumuladas sobre la fila existente del equipo, o una nueva
    private FilaTabla toFilaTabla(Long competenciaId, EstadisticasEquipo e) {
        FilaTabla fila = filaTablaRepository.findByCompetenciaIdAndEquipoId(competenciaId, e.equipoId)
                .orElseGet(FilaTabla::new);
        fila.setCompetenciaId(competenciaId);
        fila.setEquipoId(e.equipoId);
        fila.setInstitucionId(e.institucionId);
        fila.setPj(e.pj);
        fila.setPg(e.pg);
        fila.setPe(e.pe);
        fila.setPp(e.pp);
        fila.setPuntosFavor(e.puntosFavor);
        fila.setPuntosContra(e.puntosContra);
        fila.setDiferencia(e.puntosFavor - e.puntosContra);
        fila.setPts(e.pts);
        return fila;
    }

    private static class EstadisticasEquipo {
        final Long equipoId;
        final Long institucionId;
        int pj;
        int pg;
        int pe;
        int pp;
        int puntosFavor;
        int puntosContra;
        int pts;

        EstadisticasEquipo(Long equipoId, Long institucionId) {
            this.equipoId = equipoId;
            this.institucionId = institucionId;
        }
    }
}
