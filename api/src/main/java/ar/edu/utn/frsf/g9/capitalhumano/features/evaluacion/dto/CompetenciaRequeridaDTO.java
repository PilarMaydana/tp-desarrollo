package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO informativo de una competencia requerida por un puesto junto con su ponderación (CU 025 - Paso 9).
 */
@Schema(description = "Competencia requerida por un puesto y su ponderación")
public record CompetenciaRequeridaDTO(
        @Schema(description = "Identificador de la competencia", example = "1")
        Long competenciaId,

        @Schema(description = "Código de la competencia", example = "COM-01")
        String codigo,

        @Schema(description = "Nombre de la competencia", example = "COMUNICACIÓN")
        String nombre,

        @Schema(description = "Ponderación requerida por el puesto", example = "8.0")
        Double ponderacionRequerida
) {
}
