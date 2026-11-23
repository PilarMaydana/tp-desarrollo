package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Resumen de una evaluación asociada a un puesto para la selección previa a emitir el orden de mérito
 * (CU 028 - Paso 6).
 *
 * @param evaluacionId       identificador de la evaluación
 * @param fechaCreacion      fecha y hora de creación de la evaluación
 * @param cantidadCandidatos cantidad total de cuestionarios (candidatos) en la evaluación
 * @param cantidadCompletos  cantidad de cuestionarios en estado COMPLETO dentro de la evaluación
 */
@Schema(description = "Evaluación disponible para emitir el orden de mérito de un puesto")
public record EvaluacionOrdenMeritoDTO(
        Long evaluacionId,
        LocalDateTime fechaCreacion,
        long cantidadCandidatos,
        long cantidadCompletos
) {
}
