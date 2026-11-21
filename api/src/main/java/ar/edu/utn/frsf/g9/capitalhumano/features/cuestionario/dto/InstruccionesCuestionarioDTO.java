package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;

/**
 * DTO de respuesta para la pantalla "Cuestionario – Instrucciones" (CU 026 - Paso 1).
 *
 * @param id                  identificador del cuestionario
 * @param estado              estado actual del cuestionario (ACTIVO o EN_PROCESO)
 * @param nombrePuesto        nombre del puesto o función evaluado
 * @param tiempoMaximoMinutos tiempo máximo permitido para completar el cuestionario en minutos
 * @param instrucciones       texto de instrucciones generales obtenido de la configuración del sistema
 */
public record InstruccionesCuestionarioDTO(
        Long id,
        EstadoCuestionario estado,
        String nombrePuesto,
        Integer tiempoMaximoMinutos,
        String instrucciones
) {
}
