# athenet-ranking

Microservicio de ranking de la plataforma Athenet (MS-Ranking).

Es una proyección de solo lectura (CQRS ligero): no recibe resultados por REST ni calcula nada bajo demanda. Escucha el mensaje `resultado.registrado`, publicado por MS-Competencia a través de RabbitMQ, y a partir de ahí calcula y mantiene:

- La **tabla de posiciones** de cada competencia (liga).
- El **ranking histórico de equipos**, acumulado entre competencias, por deporte.

## Stack

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA + PostgreSQL (esquema versionado con Flyway)
- Spring AMQP (RabbitMQ), con retry + dead-letter queue
- Bean Validation
- Spring Boot Actuator (`/actuator/health`)
- Lombok

## Puerto

`8083`

## Despliegue

Ver [DEPLOY.md](./DEPLOY.md): Docker, variables de entorno (`.env`), Flyway y cómo correr los tests.

## Contrato de resultados

Se aceptan campos adicionales del productor (nombres y ronda), se valida el sobre, el tipo y una revisión positiva, y se admite `matchDate` ISO opcional. Una corrección sustituye el snapshot del encuentro y recalcula; versiones repetidas o anteriores no se aplican.

`RABBITMQ_VHOST` debe coincidir con el resto de servicios. Se conserva la cola `ranking.resultado.registrado.queue`. `RANKING_DLX_ROUTING_KEY` vacío conserva sus argumentos y binding actuales; tras inventariar/migrar el broker puede configurarse una clave exclusiva, por ejemplo `ranking.resultado.registrado.queue.dlq`. No cambiarla sobre una cola existente sin una migración que preserve mensajes. Mantener una sola instancia consumidora hasta verificar concurrencia del recálculo compartido.
