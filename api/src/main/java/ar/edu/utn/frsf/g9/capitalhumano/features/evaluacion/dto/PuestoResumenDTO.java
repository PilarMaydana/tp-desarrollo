package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO resumido de un Puesto o Función activo para selección en el CU 025 (Paso 8).
 */
@Schema(description = "Resumen de un puesto activo disponible para evaluación")
public record PuestoResumenDTO(
        @Schema(description = "Identificador único del puesto", example = "1")
        Long id,

        @Schema(description = "Código único del puesto", example = "DEV-01")
        String codigo,

        @Schema(description = "Nombre del puesto o función", example = "DESARROLLADOR SENIOR")
        String nombre,

        @Schema(description = "Empresa solicitante", example = "TECH SOLUTIONS")
        String empresa
) {
}
