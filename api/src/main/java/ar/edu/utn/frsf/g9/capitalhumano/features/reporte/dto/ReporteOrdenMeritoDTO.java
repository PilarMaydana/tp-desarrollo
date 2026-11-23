package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reporte completo de Orden de Mérito emitido para un puesto y un conjunto de evaluaciones
 * (CU 028 - Paso 7).
 */
@Schema(description = "Reporte completo de Orden de Mérito para un puesto")
public record ReporteOrdenMeritoDTO(
        PuestoReporteDTO puesto,
        List<EvaluacionIncluidaDTO> evaluacionesIncluidas,
        LocalDateTime fechaEmision,
        List<CompetenciaRequeridaReporteDTO> competenciasRequeridas,
        List<FilaOrdenMeritoDTO> ordenDeMerito,
        List<FilaNoAlcanzoMinimoDTO> noAlcanzaronMinimos,
        List<FilaCandidatoReporteDTO> incompletos,
        List<FilaCandidatoReporteDTO> enProceso,
        List<FilaCandidatoReporteDTO> sinContestar,
        List<FilaCandidatoReporteDTO> activos,
        TotalesReporteDTO totales
) {
    public ReporteOrdenMeritoDTO {
        evaluacionesIncluidas = evaluacionesIncluidas != null ? List.copyOf(evaluacionesIncluidas) : List.of();
        competenciasRequeridas = competenciasRequeridas != null ? List.copyOf(competenciasRequeridas) : List.of();
        ordenDeMerito = ordenDeMerito != null ? List.copyOf(ordenDeMerito) : List.of();
        noAlcanzaronMinimos = noAlcanzaronMinimos != null ? List.copyOf(noAlcanzaronMinimos) : List.of();
        incompletos = incompletos != null ? List.copyOf(incompletos) : List.of();
        enProceso = enProceso != null ? List.copyOf(enProceso) : List.of();
        sinContestar = sinContestar != null ? List.copyOf(sinContestar) : List.of();
        activos = activos != null ? List.copyOf(activos) : List.of();
    }
}
