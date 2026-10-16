package com.athenet.ranking.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una fila de la tabla de posiciones de una competencia
 *
 * Un equipo puede tener una fila por
 * cada competencia en la que participa, lo único que identifica
 * una fila de forma única es el par (competenciaId, equipoId).
 * id autogenerado
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(
        name = "fila_tabla",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_fila_tabla_competencia_equipo",
                columnNames = {"competencia_id", "equipo_id"}
        )
)
@Schema(description = "Entidad que representa una fila de la tabla de posiciones de una competencia")
public class FilaTabla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la fila")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Identificador de la competencia a la que pertenece esta fila")
    private Long competenciaId;

    @Column(nullable = false)
    @Schema(description = "Identificador del equipo al que pertenece esta fila")
    private Long equipoId;

    @Column(nullable = false)
    @Schema(description = "Identificador de la institución del equipo")
    private Long institucionId;

    @Schema(description = "Partidos jugados")
    private Integer pj;

    @Schema(description = "Partidos ganados")
    private Integer pg;

    @Schema(description = "Partidos empatados")
    private Integer pe;

    @Schema(description = "Partidos perdidos")
    private Integer pp;

    @Schema(description = "Puntaje a favor (goles, puntos, etc. según el deporte)")
    private Integer puntosFavor;

    @Schema(description = "Puntaje en contra (goles, puntos, etc. según el deporte)")
    private Integer puntosContra;

    @Schema(description = "Diferencia de puntaje (puntosFavor - puntosContra)")
    private Integer diferencia;

    @Schema(description = "Puntos acumulados en la competencia")
    private Integer pts;

    @Schema(description = "Posición del equipo en la tabla")
    private Integer posicion;
}
