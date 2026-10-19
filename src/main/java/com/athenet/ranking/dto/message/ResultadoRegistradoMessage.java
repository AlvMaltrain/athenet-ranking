package com.athenet.ranking.dto.message;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;

/**
 * Payload de datos del evento `resultado.registrado` publicado por MS-Competencia
 * (va dentro de ResultadoRegistradoEnvelope.data). Las validaciones son defensivas:
 * solo cubren lo que rompería los cálculos de ranking (NPE al desempaquetar un campo
 * nulo, etc.), no la lógica de negocio de Competencia.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResultadoRegistradoMessage(
        @NotNull @Positive Long matchId,
        @NotNull @Positive Long competitionId,
        @NotNull @Positive Long sportId,
        Long eventId,
        @Min(1) int version,
        @NotNull @Positive Long homeTeamId,
        @NotNull @Positive Long awayTeamId,
        @NotNull @Positive Long homeInstitutionId,
        @NotNull @Positive Long awayInstitutionId,
        @NotNull @PositiveOrZero Integer homeScore,
        @NotNull @PositiveOrZero Integer awayScore,
        boolean simulated,
        @NotNull @Valid Points points,
        // Fecha del partido (no la fecha en que se publico el evento). Sin @NotNull
        // a proposito: si Competencia aun no la envia, el resultado se sigue procesando
        // igual, solo que sin fecha para ordenar el "ultimo encuentro".
        LocalDate matchDate
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Points(
            @NotNull @PositiveOrZero Integer win,
            @NotNull @PositiveOrZero Integer draw,
            @NotNull @PositiveOrZero Integer loss
    ) {
    }
}
