package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO que contiene las estadísticas del resultado de la importación masiva de candidatos.
 */
@Schema(description = "Estadísticas resultantes del procesamiento del archivo de candidatos")
public record ImportacionCandidatosResponseDTO(
        @Schema(description = "Cantidad de nuevos candidatos dados de alta", example = "15")
        int candidatosCreados,

        @Schema(description = "Cantidad de candidatos existentes que fueron modificados o reactivados", example = "4")
        int candidatosActualizados,

        @Schema(description = "Cantidad de candidatos que fueron marcados con baja lógica", example = "2")
        int candidatosEliminados
) {
}
