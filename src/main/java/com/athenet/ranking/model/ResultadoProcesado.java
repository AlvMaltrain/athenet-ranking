package com.athenet.ranking.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Copia local del resultado de un partido, tal como llega en el mensaje
 * `resultado.registrado` publicado por MS-Competencia.
 *
 *id NO se genera aquí: es el mismo `partidoId` que usa Competencia,
 * porque este registro es una proyección de un dato cuya fuente de verdad
 * vive en otro servicio
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "partidoId")
@Entity
@Table(name = "resultado_procesado")
@Schema(description = "Entidad que representa el resultado ya procesado de un partido, replicado desde MS-Competencia")
public class ResultadoProcesado {

    @Id
    @Schema(description = "Identificador del partido (igual al id usado en MS-Competencia, no se genera en este servicio)")
    private Long partidoId;

    @Column(nullable = false)
    @Schema(description = "Identificador de la competencia a la que pertenece el partido")
    private Long competenciaId;

    @Column(nullable = false)
    @Schema(description = "Identificador del deporte del partido")
    private Long deporteId;

    @Column(nullable = false)
    @Schema(description = "Versión del mensaje, usada para descartar duplicados o mensajes fuera de orden")
    private Integer version;

    @Column(nullable = false)
    @Schema(description = "Identificador del equipo local")
    private Long equipoLocalId;

    @Column(nullable = false)
    @Schema(description = "Identificador del equipo visita")
    private Long equipoVisitaId;

    @Column(nullable = false)
    @Schema(description = "Identificador de la institución del equipo local")
    private Long institucionLocalId;

    @Column(nullable = false)
    @Schema(description = "Identificador de la institución del equipo visita")
    private Long institucionVisitaId;

    @Schema(description = "Puntaje anotado por el equipo local (goles, puntos, etc. según el deporte)")
    private Integer puntajeLocal;

    @Schema(description = "Puntaje anotado por el equipo visita (goles, puntos, etc. según el deporte)")
    private Integer puntajeVisita;

    @Schema(description = "Puntos otorgados por una victoria")
    private Integer puntosVictoria;

    @Schema(description = "Puntos otorgados por un empate")
    private Integer puntosEmpate;

    @Schema(description = "Puntos otorgados por una derrota")
    private Integer puntosDerrota;
}
