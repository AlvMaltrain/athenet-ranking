package com.athenet.ranking.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;

class RabbitTopologyTest {
    RabbitConfig config(String dlqRoutingKey) {
        var config = new RabbitConfig();
        ReflectionTestUtils.setField(config,"queueName","ranking.resultado.registrado.queue");
        ReflectionTestUtils.setField(config,"dlqName","ranking.resultado.registrado.queue.dlq");
        ReflectionTestUtils.setField(config,"routingKey","resultado.registrado");
        ReflectionTestUtils.setField(config,"deadLetterExchangeName","athenet.dlx");
        ReflectionTestUtils.setField(config,"deadLetterRoutingKey",dlqRoutingKey);
        return config;
    }
    @Test void legacyQueueArgumentsRemainCompatibleByDefault() {
        var config = config("");
        assertThat(config.resultadoRegistradoQueue().getArguments()).doesNotContainKey("x-dead-letter-routing-key");
        assertThat(config.resultadoRegistradoDlqBinding(config.resultadoRegistradoDlq(),config.deadLetterExchange()).getRoutingKey())
                .isEqualTo("resultado.registrado");
    }
    @Test void isolatedDeadLetterKeyCanBeActivatedAfterBrokerMigration() {
        var config = config("ranking.resultado.registrado.queue.dlq");
        assertThat(config.resultadoRegistradoQueue().getArguments()).containsEntry("x-dead-letter-routing-key","ranking.resultado.registrado.queue.dlq");
        assertThat(config.resultadoRegistradoDlqBinding(config.resultadoRegistradoDlq(),config.deadLetterExchange()).getRoutingKey())
                .isEqualTo("ranking.resultado.registrado.queue.dlq");
    }
}
