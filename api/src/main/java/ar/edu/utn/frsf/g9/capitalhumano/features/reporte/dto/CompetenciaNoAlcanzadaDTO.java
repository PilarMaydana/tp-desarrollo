package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Detalle de una competencia del puesto cuyo mínimo no fue alcanzado por un candidato
 * con cuestionario COMPLETO.
 *
 * @param competenciaId identificador de la competencia
 * @param codigo        código de la competencia
 * @param competencia   nombre de la competencia
 * @param obtenido      puntaje obtenido en la competencia ({@code null} si la competencia no figura en el resultado)
 * @param requerido     ponderación mínima requerida por el puesto
 */
@Schema(description = "Competencia requerida que el candidato no alcanzó")
public record CompetenciaNoAlcanzadaDTO(
        Long competenciaId,
        String codigo,
        String competencia,
        @Schema(description = "Puntaje obtenido en la competencia (null si faltó en el resultado)", nullable = true)
        BigDecimal obtenido,
        BigDecimal requerido
) {
}
