package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import java.util.List;

/**
 * DTO que representa un bloque (paso del wizard) de un cuestionario (CU 026).
 *
 * @param id         identificador del bloque
 * @param numero     número de bloque (1..N)
 * @param completado indica si el bloque ya fue respondido en su totalidad
 * @param preguntas  lista ordenada de preguntas asignadas al bloque
 */
public record BloqueCuestionarioDTO(
        Long id,
        Integer numero,
        boolean completado,
        List<PreguntaAsignadaDTO> preguntas
) {
}
