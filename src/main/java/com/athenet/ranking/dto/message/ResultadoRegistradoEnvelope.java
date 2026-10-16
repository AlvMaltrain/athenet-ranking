package com.athenet.ranking.dto.message;

/**
 * Sobre del evento `resultado.registrado`: Competencia envuelve el payload
 * real en este record 
 */
public record ResultadoRegistradoEnvelope(
        String messageId,
        String occurredAt,
        String type,
        ResultadoRegistradoMessage data
) {
}
