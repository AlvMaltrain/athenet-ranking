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
 * Ranking histórico de un equipo en un deporte, acumulado entre todas las
 * competencias en las que ha participado (no se resetea por competencia,
 * a diferencia de FilaTabla).
 *
 * Igual que en FilaTabla, la clave real es el par (deporteId, equipoId),
 * reforzada con una restricción única a nivel de tabla.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(
        name = "ranking_historico",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ranking_historico_deporte_equipo",
                columnNames = {"deporte_id", "equipo_id"}
        )
)
@Schema(description = "Entidad que representa el ranking histórico de un equipo en un deporte, acumulado entre competencias")
public class RankingHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la fila")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Identificador del deporte")
    private Long deporteId;

    @Column(nullable = false)
    @Schema(description = "Identificador del equipo")
    private Long equipoId;

    @Column(nullable = false)
    @Schema(description = "Identificador de la institución del equipo")
    private Long institucionId;

    @Schema(description = "Partidos jugados (histórico, todas las competencias)")
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

    @Schema(description = "Puntos acumulados en todo el histórico del deporte")
    private Integer pts;

    @Schema(description = "Posición del equipo en el ranking histórico del deporte")
    private Integer posicion;
}
