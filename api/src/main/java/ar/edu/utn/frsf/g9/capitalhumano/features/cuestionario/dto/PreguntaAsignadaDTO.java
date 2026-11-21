package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import java.util.List;

/**
 * DTO de una pregunta asignada dentro del bloque actual de un cuestionario (CU 026).
 *
 * @param preguntaAsignadaId identificador de la pregunta asignada al bloque
 * @param nroOrden           número de orden secuencial de la pregunta en el cuestionario
 * @param enunciado          enunciado de la pregunta
 * @param opciones           opciones de respuesta ordenadas sin ponderaciones
 * @param respuestaOpcionId  identificador de la opción ya respondida por el candidato, o {@code null} si aún no fue respondida
 */
public record PreguntaAsignadaDTO(
        Long preguntaAsignadaId,
        Integer nroOrden,
        String enunciado,
        List<OpcionPreguntaCandidatoDTO> opciones,
        Long respuestaOpcionId
) {
}
