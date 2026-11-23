package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Competencia requerida por el puesto y su ponderación mínima exigida.
 */
@Schema(description = "Competencia requerida por el puesto con su ponderación mínima")
public record CompetenciaRequeridaReporteDTO(
        Long competenciaId,
        String codigo,
        String nombre,
        BigDecimal ponderacionRequerida
) {
}
