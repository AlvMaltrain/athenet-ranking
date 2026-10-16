# ---- Etapa 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos primero solo el pom para cachear las dependencias en una capa
# separada del codigo fuente (un cambio en el codigo no obliga a volver a
# descargar todo Maven Central).
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Etapa 2: runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios: la app no corre como root dentro del contenedor.
RUN addgroup -S athenet && adduser -S athenet -G athenet

COPY --from=build /app/target/*.jar app.jar
RUN chown athenet:athenet app.jar
USER athenet

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
