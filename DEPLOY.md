# Despliegue — athenet.ranking

## Variables de entorno

Copia `.env.example` a `.env` (mismo directorio) y completa los valores
reales. `.env` ya esta en `.gitignore`: nunca se commitea.

```bash
cp .env.example .env
# edita .env con tus valores locales
```

Ya no hay contraseñas (`DB_PASSWORD`, `RABBITMQ_PASSWORD`) con un valor por
defecto en `application.yaml`: hay que proveerlas siempre, para no dejar
credenciales reales commiteadas en el repo.

## Correr todo en local con Docker

```bash
docker compose up --build -d   # build local desde el Dockerfile + db + rabbitmq
docker compose logs -f app     # ver logs
docker compose down            # apagar todo (sin -v para conservar los datos)
```

`docker compose` carga `.env` automaticamente porque esta en el mismo
directorio que `docker-compose.yml`.

## Correr el backend SIN Docker (como antes, pero exportando las variables)

Como `DB_PASSWORD` y `RABBITMQ_PASSWORD` ya no tienen un valor por defecto
en `application.yaml`, hay que exportarlas antes de levantar la app (o
configurarlas como variables de entorno en el IDE). Si ya tienes `.env`
creado, puedes cargarlo directo en la shell:

```bash
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run
```

(`db` y `rabbitmq` deben seguir corriendo via Docker para esto —
`docker compose up -d db rabbitmq` — solo la app corre fuera del contenedor.)

## Esquema de base de datos (Flyway)

El esquema ya no lo gestiona Hibernate (`ddl-auto` paso de `update` a
`validate`): la fuente de verdad es la migracion
`src/main/resources/db/migration/V1__init.sql`, que Flyway aplica
automaticamente al arrancar. Un cambio de esquema a futuro va en una nueva
migracion (`V2__...sql`), nunca editando `V1` ya aplicada.

## Health check

Con `spring-boot-starter-actuator`, el endpoint `GET /actuator/health`
expone el estado de la app (incluye liveness/readiness, utiles para un
orquestador de contenedores). `GET /actuator/info` tambien esta habilitado.

## Tests

```bash
./mvnw test
```

`TablaPosicionesServiceTest` y `RankingHistoricoServiceTest` son pruebas
unitarias con Mockito (no requieren Postgres/RabbitMQ corriendo).
`RankingApplicationTests` (el test de contexto por defecto) si los
requiere, porque levanta toda la aplicacion.

## Broker compartido (RabbitMQ)

- `RABBITMQ_VHOST` (default `/`): debe coincidir con el vhost del resto de servicios.
- `RANKING_DLX_ROUTING_KEY` (default vacio): clave de dead-letter exclusiva de Ranking. Vacio conserva la
  topologia anterior. En un broker nuevo usar `ranking.resultado.registrado.queue.dlq` desde el primer arranque;
  cambiarla sobre una cola existente falla con `PRECONDITION_FAILED` (requiere migrar la cola).
- Mantener una sola instancia consumidora hasta verificar la concurrencia del recalculo.

## CI/CD y despliegue en EC2

El workflow `.github/workflows/ci-cd.yml` hace lo mismo que el de institution:

1. PR o push a `main`/`develop` -> `build-and-test` (`./mvnw clean verify`, con Postgres y
   RabbitMQ como contenedores de servicio, porque `RankingApplicationTests` los necesita).
2. Push a `main` -> publica la imagen en GHCR (`ghcr.io/alvmaltrain/athenet-ranking`, tags
   `latest` y `<sha>`) y despliega por SSH en la EC2 (`docker compose pull app && up -d`).

### Secrets del repo (Settings -> Secrets and variables -> Actions)

| Secret        | Valor                                                       |
|---------------|-------------------------------------------------------------|
| `EC2_HOST`    | IP publica (o DNS) de la EC2 de ranking                     |
| `EC2_USER`    | `ec2-user` (Amazon Linux) o `ubuntu` (Ubuntu)               |
| `EC2_SSH_KEY` | Contenido completo del `.pem` (incluye las lineas BEGIN/END) |

GHCR no necesita secret: el pipeline usa el `GITHUB_TOKEN` automatico.

### Setup unico de la EC2 (a mano, por SSH)

```bash
ssh -i tu-clave.pem ec2-user@<IP-EC2>

# Docker (Amazon Linux 2023)
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker $USER      # cerrar sesion y volver a entrar

# Plugin de Compose (x86_64; en instancias ARM/Graviton usar docker-compose-linux-aarch64)
DOCKER_CONFIG=${DOCKER_CONFIG:-$HOME/.docker}
mkdir -p $DOCKER_CONFIG/cli-plugins
curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
  -o $DOCKER_CONFIG/cli-plugins/docker-compose
chmod +x $DOCKER_CONFIG/cli-plugins/docker-compose
docker compose version

# Carpeta del deploy
sudo mkdir -p /opt/athenet-ranking
sudo chown $USER:$USER /opt/athenet-ranking
```

Desde tu maquina, copia el compose de produccion **con el nombre `docker-compose.yml`**
(no incluye RabbitMQ local ni publica el puerto de la base):

```bash
scp -i tu-clave.pem docker-compose.prod.yml ec2-user@<IP-EC2>:/opt/athenet-ranking/docker-compose.yml
```

Si cambias `docker-compose.prod.yml` en el repo, hay que volver a copiarlo (el pipeline
solo actualiza la imagen, no este archivo).

En la EC2 crea `/opt/athenet-ranking/.env` (no se sube a git):

```env
DB_NAME=ranking_db
DB_USERNAME=postgres
DB_PASSWORD=<clave-nueva-y-segura>

RABBITMQ_HOST=<host-del-broker>
RABBITMQ_PORT=5672
RABBITMQ_USER=<usuario-del-broker>
RABBITMQ_PASSWORD=<clave-del-broker>
RABBITMQ_VHOST=/
RABBITMQ_SSL=false
# Broker nuevo: fijar desde el primer arranque y no cambiarla despues.
RANKING_DLX_ROUTING_KEY=ranking.resultado.registrado.queue.dlq

CORS_ALLOWED_ORIGINS=https://fabetabilo.github.io
IMAGE=ghcr.io/alvmaltrain/athenet-ranking:latest
```

Security Group de la instancia: abrir TCP 8083 (entrada) y SSH 22 solo desde tu IP.

Primer arranque: no hace falta hacerlo a mano. Con la EC2 preparada (Docker, compose, `.env`,
Security Group) y los 3 secrets creados, el primer push a `main` publica la imagen y el job
`deploy` hace `compose pull` + `up -d` desde cero. Despues se verifica en la EC2:

```bash
cd /opt/athenet-ranking
docker compose ps
docker compose logs -f app
curl http://localhost:8083/actuator/health    # debe responder {"status":"UP"}
```
