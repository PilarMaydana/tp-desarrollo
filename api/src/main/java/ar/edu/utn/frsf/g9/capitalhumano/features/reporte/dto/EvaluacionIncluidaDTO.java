package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Identificador y fecha de cada evaluación incluida en el reporte de orden de mérito.
 */
@Schema(description = "Evaluación incluida en el reporte")
public record EvaluacionIncluidaDTO(
        Long evaluacionId,
        LocalDateTime fechaCreacion
) {
}
