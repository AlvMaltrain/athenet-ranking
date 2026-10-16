package com.athenet.ranking.listener;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.athenet.ranking.dto.message.ResultadoRegistradoEnvelope;
import com.athenet.ranking.dto.message.ResultadoRegistradoMessage;
import com.athenet.ranking.service.ResultadoProcesadoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Escucha `resultado.registrado` y delega el procesamiento al service.
 * Recibe el Message crudo y deserializa a mano (ver RabbitConfig.rabbitObjectMapper):
 * la conversión automática a un record tipado no funciona en esta combinación de versiones.
 */
@Component
public class ResultadoRegistradoListener {

    private static final Logger log = LoggerFactory.getLogger(ResultadoRegistradoListener.class);

    private final ResultadoProcesadoService resultadoProcesadoService;
    private final ObjectMapper rabbitObjectMapper;
    private final Validator validator;

    public ResultadoRegistradoListener(ResultadoProcesadoService resultadoProcesadoService,
            ObjectMapper rabbitObjectMapper, Validator validator) {
        this.resultadoProcesadoService = resultadoProcesadoService;
        this.rabbitObjectMapper = rabbitObjectMapper;
        this.validator = validator;
    }

    @RabbitListener(queues = "${athenet.messaging.resultado-registrado.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void escuchar(Message message) throws IOException {
        ResultadoRegistradoEnvelope envelope = rabbitObjectMapper.readValue(message.getBody(), ResultadoRegistradoEnvelope.class);
        ResultadoRegistradoMessage mensaje = envelope.data();

        validar(mensaje);

        log.info("Resultado recibido: partido {} (version {})", mensaje.matchId(), mensaje.version());
        resultadoProcesadoService.procesar(mensaje);
    }

    // Validacion defensiva: evita que un campo nulo/invalido rompa los calculos de la tabla
    // (ej. NPE al desempaquetar un score null) con un error claro en el log en vez de eso.
    private void validar(ResultadoRegistradoMessage mensaje) {
        Set<ConstraintViolation<ResultadoRegistradoMessage>> violaciones = validator.validate(mensaje);
        if (!violaciones.isEmpty()) {
            throw new IllegalArgumentException("Mensaje resultado.registrado invalido: " + violaciones);
        }
    }
}
