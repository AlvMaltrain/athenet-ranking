package com.athenet.ranking.dto.message;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Payload de datos del evento `resultado.registrado` publicado por MS-Competencia
 * (dentro de ResultadoRegistradoEnvelope.data).Validaciones defensivas:
 * solo cubren lo que rompería los cálculos de ranking (NPE al desempaquetar un campo
 * nulo, etc.), no la lógica de negocio de Competencia.
 */
public record ResultadoRegistradoMessage(
        @NotNull Long matchId,
        @NotNull Long competitionId,
        @NotNull Long sportId,
        Long eventId,
        int version,
        @NotNull Long homeTeamId,
        @NotNull Long awayTeamId,
        @NotNull Long homeInstitutionId,
        @NotNull Long awayInstitutionId,
        @NotNull @PositiveOrZero Integer homeScore,
        @NotNull @PositiveOrZero Integer awayScore,
        boolean simulated,
        @NotNull @Valid Points points
) {
    public record Points(
            @PositiveOrZero int win,
            @PositiveOrZero int draw,
            @PositiveOrZero int loss
    ) {
    }
}
