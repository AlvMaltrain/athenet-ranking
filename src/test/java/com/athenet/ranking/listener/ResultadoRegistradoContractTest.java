package com.athenet.ranking.listener;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import com.athenet.ranking.dto.message.ResultadoRegistradoMessage;
import com.athenet.ranking.service.ResultadoProcesadoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

/** Muestras del productor conservadas por Notificaciones; incluye campos ajenos al cálculo. */
class ResultadoRegistradoContractTest {
    final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    String fixture(int version) throws Exception {
        String actual = System.getProperty("ranking.contract.fixture-directory");
        if (actual != null) return java.nio.file.Files.readString(java.nio.file.Path.of(actual,"result-v" + version + ".json"));
        try (var input = getClass().getResourceAsStream("/contracts/result-v" + version + ".json")) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    Message message(String json) {
        var properties = new MessageProperties();
        properties.setHeader("__TypeId__", "com.athenet.competition.messaging.ResultRegisteredEnvelope");
        return new Message(json.getBytes(StandardCharsets.UTF_8), properties);
    }
    @ParameterizedTest @ValueSource(ints = {1, 2})
    void acceptsProducerNamesAndRoundWithoutUsingTypeHeader(int version) throws Exception {
        var service = mock(ResultadoProcesadoService.class);
        try (var validators = Validation.buildDefaultValidatorFactory()) {
            var listener = new ResultadoRegistradoListener(service,mapper,validators.getValidator());
            listener.escuchar(message(fixture(version)));
            var result = ArgumentCaptor.forClass(ResultadoRegistradoMessage.class);
            verify(service).procesar(result.capture());
            assertThat(result.getValue().version()).isEqualTo(version);
            assertThat(result.getValue().points().win()).isEqualTo(5);
        }
    }
    @Test void acceptsOptionalMatchDateAndIgnoresFutureFields() throws Exception {
        var tree = mapper.readTree(fixture(1));
        ((com.fasterxml.jackson.databind.node.ObjectNode)tree.path("data")).put("matchDate","2026-10-18");
        ((com.fasterxml.jackson.databind.node.ObjectNode)tree).put("futureField",true);
        var service = mock(ResultadoProcesadoService.class);
        try (var validators = Validation.buildDefaultValidatorFactory()) {
            new ResultadoRegistradoListener(service,mapper,validators.getValidator()).escuchar(message(tree.toString()));
            var result = ArgumentCaptor.forClass(ResultadoRegistradoMessage.class);
            verify(service).procesar(result.capture());
            assertThat(result.getValue().matchDate()).isEqualTo(LocalDate.of(2026,10,18));
        }
    }
    @Test void invalidEnvelopeVersionAndMissingPointsNeverReachCalculation() throws Exception {
        var service = mock(ResultadoProcesadoService.class);
        try (var validators = Validation.buildDefaultValidatorFactory()) {
            var listener = new ResultadoRegistradoListener(service,mapper,validators.getValidator());
            for (String invalid : new String[]{"null", "{}", "{no es json", fixture(1).replace("resultado.registrado", "otro.tipo"),
                    fixture(1).replace("2026-10-17T22:43:12.482413122Z", "ayer")}) {
                assertThatThrownBy(() -> listener.escuchar(message(invalid))).isInstanceOf(IllegalArgumentException.class);
            }
            var tree = mapper.readTree(fixture(1));
            ((com.fasterxml.jackson.databind.node.ObjectNode)tree.path("data")).put("version",0);
            var invalidVersion = tree.toString();
            assertThatThrownBy(() -> listener.escuchar(message(invalidVersion))).isInstanceOf(IllegalArgumentException.class);
            tree = mapper.readTree(fixture(1));
            ((com.fasterxml.jackson.databind.node.ObjectNode)tree.path("data").path("points")).remove("win");
            var invalidPoints = tree.toString();
            assertThatThrownBy(() -> listener.escuchar(message(invalidPoints))).isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(service);
        }
    }
}
