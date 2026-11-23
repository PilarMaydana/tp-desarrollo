package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Totales de filas por grupo en el reporte de orden de mérito.
 */
@Schema(description = "Cantidad de filas (cuestionarios) en cada grupo del reporte")
public record TotalesReporteDTO(
        int ordenDeMerito,
        int noAlcanzaronMinimos,
        int incompletos,
        int enProceso,
        int sinContestar,
        int activos,
        int totalGeneral
) {
}
