package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

/**
 * DTO de una opción de respuesta visible para el candidato.
 * Nunca expone la ponderación interna de la opción.
 *
 * @param id          identificador de la opción de respuesta de la pregunta
 * @param orden       número de orden de presentación
 * @param descripcion texto visible de la opción
 */
public record OpcionPreguntaCandidatoDTO(
        Long id,
        Integer orden,
        String descripcion
) {
}
