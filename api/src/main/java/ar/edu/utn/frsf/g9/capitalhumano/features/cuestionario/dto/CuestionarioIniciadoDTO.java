package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;

import java.time.LocalDateTime;

/**
 * DTO de respuesta al iniciar o reingresar a un cuestionario (CU 026 - Pasos 2 a 4 y Flujo 1.C).
 *
 * @param id                identificador del cuestionario
 * @param estado            estado actual del cuestionario ({@link EstadoCuestionario#EN_PROCESO})
 * @param fechaInicio       fecha y hora de inicio del cuestionario
 * @param segundosRestantes tiempo restante en segundos hasta alcanzar el tiempo máximo de llenado
 * @param totalBloques      cantidad total de bloques del cuestionario
 * @param bloqueActual      primer bloque pendiente de responder con sus preguntas y opciones
 */
public record CuestionarioIniciadoDTO(
        Long id,
        EstadoCuestionario estado,
        LocalDateTime fechaInicio,
        long segundosRestantes,
        int totalBloques,
        BloqueCuestionarioDTO bloqueActual
) {
}
