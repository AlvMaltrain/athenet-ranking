package com.athenet.ranking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.athenet.ranking.dto.message.ResultadoRegistradoMessage;
import com.athenet.ranking.model.ResultadoProcesado;
import com.athenet.ranking.repository.ResultadoProcesadoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Aplica un resultado de forma idempotente y dispara el recálculo de la
 * tabla de posiciones de la competencia y del ranking histórico del deporte.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResultadoProcesadoService {

    private final ResultadoProcesadoRepository resultadoProcesadoRepository;
    private final TablaPosicionesService tablaPosicionesService;
    private final RankingHistoricoService rankingHistoricoService;

    public void procesar(ResultadoRegistradoMessage mensaje) {
        ResultadoProcesado existente = resultadoProcesadoRepository.findById(mensaje.matchId()).orElse(null);

        if (existente != null && existente.getVersion() >= mensaje.version()) {
            log.info("Mensaje descartado: partido {} ya aplicado con version {}", mensaje.matchId(), existente.getVersion());
            return;
        }

        ResultadoProcesado resultado = toEntity(mensaje);
        resultadoProcesadoRepository.save(resultado);

        tablaPosicionesService.recalcular(resultado.getCompetenciaId());
        rankingHistoricoService.actualizar(resultado);
    }

    private ResultadoProcesado toEntity(ResultadoRegistradoMessage m) {
        return ResultadoProcesado.builder()
                .partidoId(m.matchId())
                .competenciaId(m.competitionId())
                .deporteId(m.sportId())
                .version(m.version())
                .equipoLocalId(m.homeTeamId())
                .equipoVisitaId(m.awayTeamId())
                .institucionLocalId(m.homeInstitutionId())
                .institucionVisitaId(m.awayInstitutionId())
                .puntajeLocal(m.homeScore())
                .puntajeVisita(m.awayScore())
                .puntosVictoria(m.points().win())
                .puntosEmpate(m.points().draw())
                .puntosDerrota(m.points().loss())
                .fechaPartido(m.matchDate())
                .build();
    }
}
