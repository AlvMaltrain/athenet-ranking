package com.athenet.ranking.dto.response;

import java.time.LocalDate;

/**
 * Ultimo partido procesado de un equipo (el mas reciente disponible para
 * ese equipo en su historico de deporte), para mostrar en la fila del
 * ranking sin que el front tenga que pedirlo aparte.
 */
public record UltimoEncuentroResponse(
        Long competenciaId,
        Long equipoLocalId,
        Long institucionLocalId,
        Long equipoVisitaId,
        Long institucionVisitaId,
        Integer puntajeLocal,
        Integer puntajeVisita,
        LocalDate fecha
) {
}
