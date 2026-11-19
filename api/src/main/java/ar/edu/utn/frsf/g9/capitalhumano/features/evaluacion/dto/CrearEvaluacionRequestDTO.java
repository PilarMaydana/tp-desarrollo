package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Solicitud para confirmar y persistir la evaluación junto con sus cuestionarios (CU 025 - Pasos 12 y 13).
 */
@Schema(description = "Datos para confirmar y crear una nueva evaluación")
public record CrearEvaluacionRequestDTO(
        @NotNull(message = "Debe seleccionar una función o puesto")
        @Schema(description = "Identificador del puesto o función", example = "1")
        Long puestoId,

        @NotEmpty(message = "Debe seleccionar uno o más candidatos")
        @Schema(description = "Lista de identificadores de candidatos a evaluar", example = "[3, 4]")
        List<@NotNull(message = "El identificador del candidato no puede ser nulo") Long> candidatoIds,

        @Schema(description = "Mapa opcional candidatoId -> claveAcceso (generadas en el paso de previsualización). Si se omite, el sistema genera claves aleatorias automáticamente.")
        Map<Long, String> clavesPorCandidato
) {
    public CrearEvaluacionRequestDTO(Long puestoId, List<Long> candidatoIds) {
        this(puestoId, candidatoIds, null);
    }
}
