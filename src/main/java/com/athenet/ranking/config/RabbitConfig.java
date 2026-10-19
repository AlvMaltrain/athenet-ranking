package com.athenet.ranking.config;

import java.time.Duration;
import java.util.Map;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.StatelessRetryOperationsInterceptor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Declara la cola propia de Ranking, bindeada al exchange compartido "athenet.topic",
 * mas su dead-letter queue: si el procesamiento falla 3 veces (backoff 1s/2s/4s),
 * el mensaje cae a la DLQ en vez de reencolarse en loop o perderse. Mismo patron
 * que usa Competencia en su RabbitConfig. Un IllegalArgumentException (mensaje invalido,
 * ver ResultadoRegistradoListener) se salta los reintentos: nunca va a dejar de fallar,
 * asi que va directo a la DLQ.
 */
@Configuration
public class RabbitConfig {

    @Value("${athenet.messaging.resultado-registrado.exchange}")
    private String exchangeName;

    @Value("${athenet.messaging.resultado-registrado.queue}")
    private String queueName;

    @Value("${athenet.messaging.resultado-registrado.routing-key}")
    private String routingKey;

    @Value("${athenet.messaging.resultado-registrado.dead-letter-exchange}")
    private String deadLetterExchangeName;

    @Value("${athenet.messaging.resultado-registrado.dlq}")
    private String dlqName;

    // Vacío conserva la topología existente. Activar solo tras migrar/verificar la cola del broker.
    @Value("${athenet.messaging.resultado-registrado.dead-letter-routing-key:}")
    private String deadLetterRoutingKey;

    @Bean
    public TopicExchange resultadoRegistradoExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(deadLetterExchangeName, true, false);
    }

    // Sin "x-dead-letter-routing-key": al morir, el mensaje conserva su routing key
    // original (resultado.registrado), que es la misma que usa el binding de la DLQ de abajo.
    @Bean
    public Queue resultadoRegistradoQueue() {
        return new Queue(queueName, true, false, false,
                deadLetterRoutingKey == null || deadLetterRoutingKey.isBlank()
                    ? Map.of("x-dead-letter-exchange", deadLetterExchangeName)
                    : Map.of("x-dead-letter-exchange", deadLetterExchangeName,
                            "x-dead-letter-routing-key", deadLetterRoutingKey));
    }

    @Bean
    public Queue resultadoRegistradoDlq() {
        return new Queue(dlqName, true);
    }

    @Bean
    public Binding resultadoRegistradoBinding(Queue resultadoRegistradoQueue, TopicExchange resultadoRegistradoExchange) {
        return BindingBuilder.bind(resultadoRegistradoQueue).to(resultadoRegistradoExchange).with(routingKey);
    }

    @Bean
    public Binding resultadoRegistradoDlqBinding(Queue resultadoRegistradoDlq, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(resultadoRegistradoDlq).to(deadLetterExchange)
                .with(deadLetterRoutingKey == null || deadLetterRoutingKey.isBlank() ? routingKey : deadLetterRoutingKey);
    }

    // 3 reintentos con backoff exponencial (1s, 2s, 4s) antes de rechazar el mensaje sin
    // reencolar; ahi entra el x-dead-letter-exchange de la cola y RabbitMQ lo manda a la DLQ.
    // excludes: un mensaje invalido (ver ResultadoRegistradoListener.validar) nunca va a
    // dejar de fallar reintentando, asi que va directo a la DLQ sin gastar los 3 intentos.
    @Bean
    public StatelessRetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .configureRetryPolicy(policy -> policy
                        .maxRetries(3)
                        .delay(Duration.ofSeconds(1))
                        .multiplier(2.0)
                        .maxDelay(Duration.ofSeconds(8))
                        .excludes(IllegalArgumentException.class))
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            StatelessRetryOperationsInterceptor retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }

    // ObjectMapper propio para deserializar el body del mensaje a mano en el listener: la
    // conversión automática de @RabbitListener a un record tipado falla con esta combinación
    // de versiones de Spring/Jackson (mismo problema que documentó Competencia en su RabbitConfig).
    @Bean
    public ObjectMapper rabbitObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
