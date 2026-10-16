package com.athenet.ranking.dto.message;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Payload de datos del evento `resultado.registrado` publicado por MS-Competencia
 * (va dentro de ResultadoRegistradoEnvelope.data). Las validaciones son defensivas:
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
        @NotNull @Valid Points points,
        // Fecha del partido (no la fecha en que se publico el evento). Sin @NotNull
        // a proposito: si Competencia aun no la envia, el resultado se sigue procesando
        // igual, solo que sin fecha para ordenar el "ultimo encuentro".
        LocalDate matchDate
) {
    public record Points(
            @PositiveOrZero int win,
            @PositiveOrZero int draw,
            @PositiveOrZero int loss
    ) {
    }
}
