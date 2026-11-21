package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO de entrada para enviar las respuestas de un bloque de cuestionario (CU 026 - Pasos 5 a 8).
 *
 * @param respuestas lista de respuestas elegidas para las preguntas del bloque actual
 */
@Schema(description = "Respuestas enviadas por el candidato para un bloque del cuestionario")
public record ResponderBloqueRequestDTO(
        @Schema(description = "Lista de respuestas correspondientes a las preguntas del bloque")
        @NotNull(message = "La lista de respuestas es obligatoria")
        @Valid
        List<RespuestaItemDTO> respuestas
) {
}
