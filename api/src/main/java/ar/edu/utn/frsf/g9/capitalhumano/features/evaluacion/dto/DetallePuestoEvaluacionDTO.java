package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Detalle informativo de un puesto y las competencias evaluables asociadas (CU 025 - Paso 9).
 */
@Schema(description = "Detalle de puesto con las competencias y ponderaciones requeridas")
public record DetallePuestoEvaluacionDTO(
        @Schema(description = "Identificador único del puesto", example = "1")
        Long puestoId,

        @Schema(description = "Código del puesto", example = "DEV-01")
        String codigo,

        @Schema(description = "Nombre del puesto o función", example = "DESARROLLADOR SENIOR")
        String nombre,

        @Schema(description = "Empresa solicitante", example = "TECH SOLUTIONS")
        String empresa,

        @Schema(description = "Competencias asociadas al puesto y sus ponderaciones requeridas")
        List<CompetenciaRequeridaDTO> competencias
) {
}
