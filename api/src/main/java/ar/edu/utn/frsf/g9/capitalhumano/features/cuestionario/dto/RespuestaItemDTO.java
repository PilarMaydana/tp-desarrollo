package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO que representa la respuesta seleccionada por el candidato para una pregunta asignada del bloque
 * (CU 026 - Paso 5).
 *
 * @param preguntaAsignadaId identificador de la pregunta asignada dentro del bloque
 * @param opcionId           identificador de la opción elegida (puede ser {@code null} si quedó sin responder, en cuyo caso se informa por Flujo 6.A)
 */
@Schema(description = "Respuesta elegida por el candidato para una pregunta asignada")
public record RespuestaItemDTO(
        @Schema(description = "Identificador de la pregunta asignada al bloque", example = "1")
        @NotNull(message = "El identificador de la pregunta asignada es obligatorio")
        Long preguntaAsignadaId,

        @Schema(description = "Identificador de la opción de respuesta elegida", example = "1")
        Long opcionId
) {
}
