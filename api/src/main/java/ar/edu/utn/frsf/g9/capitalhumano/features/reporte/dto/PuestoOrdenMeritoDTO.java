package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resumen de un puesto retornado en la búsqueda del CU 028 (Pasos 2 a 4).
 *
 * @param puestoId              identificador del puesto
 * @param codigo                código del puesto
 * @param nombre                nombre del puesto
 * @param empresa               empresa asociada al puesto
 * @param cantidadCandidatos    cantidad total de cuestionarios (candidatos) en las evaluaciones del puesto
 * @param evaluacionesCompletas cantidad de evaluaciones del puesto con todos sus cuestionarios en estado COMPLETO
 * @param evaluacionesTotal     cantidad total de evaluaciones asociadas al puesto
 */
@Schema(description = "Puesto coincidente con los criterios de búsqueda del CU 028")
public record PuestoOrdenMeritoDTO(
        Long puestoId,
        String codigo,
        String nombre,
        String empresa,
        long cantidadCandidatos,
        long evaluacionesCompletas,
        long evaluacionesTotal
) {
}
