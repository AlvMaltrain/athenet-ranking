# athenet-ranking

Microservicio de ranking de la plataforma Athenet (MS-Ranking).

Es una proyección de solo lectura (CQRS ligero): no recibe resultados por REST ni calcula nada bajo demanda. Escucha el mensaje `resultado.registrado`, publicado por MS-Competencia a través de RabbitMQ, y a partir de ahí calcula y mantiene:

- La **tabla de posiciones** de cada competencia (liga).
- El **ranking histórico de equipos**, acumulado entre competencias, por deporte.

## Stack

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA + PostgreSQL
- Spring AMQP (RabbitMQ)
- Bean Validation
- Lombok

## Puerto

`8083`
