package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Solicitud para validar la selección de candidatos antes de avanzar en el wizard del CU 025 (Pasos 4.A, 6 y 6.A).
 */
@Schema(description = "Lista de IDs de candidatos seleccionados para evaluar")
public record ValidarCandidatosRequestDTO(
        @NotEmpty(message = "Debe seleccionar uno o más candidatos")
        @Schema(description = "Identificadores de candidatos seleccionados", example = "[3, 4]")
        List<@NotNull(message = "El identificador del candidato no puede ser nulo") Long> candidatoIds
) {
}
