package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta al enviar las respuestas de un bloque del cuestionario (CU 026 - Pasos 7, 8 y 8.A).
 *
 * <p>Nunca expone puntajes ni ponderaciones al candidato.</p>
 *
 * @param completado        {@code false} si quedan bloques pendientes (Flujo 8.A), {@code true} si finalizó el cuestionario (Paso 8)
 * @param bloqueSiguiente   siguiente bloque pendiente de responder (presente solo cuando {@code completado = false})
 * @param segundosRestantes segundos restantes para completar el cuestionario (presente solo cuando {@code completado = false})
 * @param estado            estado final {@link EstadoCuestionario#COMPLETO} (presente solo cuando {@code completado = true})
 * @param mensaje           mensaje de confirmación de finalización (presente solo cuando {@code completado = true})
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resultado del envío de respuestas de un bloque: avance al bloque siguiente o cierre exitoso")
public record ResponderBloqueResponseDTO(
        @Schema(description = "Indica si el cuestionario finalizó completamente", example = "false")
        boolean completado,

        @Schema(description = "Siguiente bloque pendiente (presente cuando completado = false)")
        BloqueCuestionarioDTO bloqueSiguiente,

        @Schema(description = "Segundos restantes para finalizar el cuestionario (presente cuando completado = false)", example = "2980")
        Long segundosRestantes,

        @Schema(description = "Estado final del cuestionario (presente cuando completado = true)", example = "COMPLETO")
        EstadoCuestionario estado,

        @Schema(description = "Mensaje de confirmación (presente cuando completado = true)", example = "El cuestionario ha sido completado con éxito")
        String mensaje
) {

    public static final String MENSAJE_COMPLETADO = "El cuestionario ha sido completado con éxito";

    public static ResponderBloqueResponseDTO siguienteBloque(BloqueCuestionarioDTO bloqueSiguiente,
                                                             long segundosRestantes) {
        return new ResponderBloqueResponseDTO(false, bloqueSiguiente, segundosRestantes, null, null);
    }

    public static ResponderBloqueResponseDTO cuestionarioCompletado() {
        return new ResponderBloqueResponseDTO(
                true,
                null,
                null,
                EstadoCuestionario.COMPLETO,
                MENSAJE_COMPLETADO
        );
    }
}
