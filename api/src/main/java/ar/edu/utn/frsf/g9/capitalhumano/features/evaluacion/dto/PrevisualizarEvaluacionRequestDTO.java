package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Solicitud de previsualización de evaluación sin persistir en base de datos (CU 025 - Paso 11).
 */
@Schema(description = "Datos necesarios para previsualizar una evaluación y generar claves temporales")
public record PrevisualizarEvaluacionRequestDTO(
        @NotNull(message = "Debe seleccionar una función o puesto")
        @Schema(description = "Identificador del puesto o función seleccionado", example = "1")
        Long puestoId,

        @NotEmpty(message = "Debe seleccionar uno o más candidatos")
        @Schema(description = "Lista de identificadores de candidatos a evaluar", example = "[3, 4]")
        List<@NotNull(message = "El identificador del candidato no puede ser nulo") Long> candidatoIds
) {
}
