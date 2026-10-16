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
        Integer posicion,
        // Null si el equipo todavia no tiene ningun resultado procesado en este deporte
        // (no deberia pasar si ya tiene una fila en el ranking historico, pero se deja
        // nullable por si acaso).
        UltimoEncuentroResponse ultimoEncuentro
) {
}
