package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Datos de encabezado del puesto en el reporte de orden de mérito (CU 028 - Paso 7).
 */
@Schema(description = "Datos del puesto incluidos en el encabezado del reporte")
public record PuestoReporteDTO(
        Long puestoId,
        String codigo,
        String nombre,
        String empresa
) {
}
