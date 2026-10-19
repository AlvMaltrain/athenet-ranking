package com.athenet.ranking.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Sobre del evento `resultado.registrado`: Competencia envuelve el payload
 * real en este record 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResultadoRegistradoEnvelope(
        @NotBlank String messageId,
        @NotBlank String occurredAt,
        @NotNull @Pattern(regexp = "resultado\\.registrado") String type,
        @NotNull @Valid ResultadoRegistradoMessage data
) {
}
