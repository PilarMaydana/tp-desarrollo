package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Fila de un cuestionario NO completo (INCOMPLETO, EN_PROCESO, SIN_CONTESTAR o ACTIVO).
 * Según la Observación (1) del CU 028, {@code puntajeObtenido} siempre es {@code null}.
 */
@Schema(description = "Candidato (cuestionario) cuyo cuestionario no fue completado")
public record FilaCandidatoReporteDTO(
        Long cuestionarioId,
        Long evaluacionId,
        TipoDocumento tipoDocumento,
        String nroDocumento,
        String apellido,
        String nombre,
        @Schema(description = "Siempre null para cuestionarios no completos (Observación 1)", nullable = true)
        BigDecimal puntajeObtenido,
        DatosAdicionalesDTO datosAdicionales
) {
}
