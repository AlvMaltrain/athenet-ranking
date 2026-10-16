package com.athenet.ranking.dto.response;

public record RankingHistoricoResponse(
        Long equipoId,
        Long institucionId,
        Integer pj,
        Integer pg,
        Integer pe,
        Integer pp,
        Integer puntosFavor,
        Integer puntosContra,
        Integer diferencia,
        Integer pts,
        Integer posicion
) {
}
