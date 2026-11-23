package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fila del grupo {@code noAlcanzaronMinimos}: cuestionario COMPLETO que no alcanzó la ponderación
 * mínima en una o más competencias requeridas por el puesto.
 */
@Schema(description = "Candidato (cuestionario) completo que no alcanzó los mínimos en al menos una competencia")
public record FilaNoAlcanzoMinimoDTO(
        Long cuestionarioId,
        Long evaluacionId,
        TipoDocumento tipoDocumento,
        String nroDocumento,
        String apellido,
        String nombre,
        @Schema(description = "Puntaje global obtenido (null solo si el cuestionario completo carece de resultado calculado)", nullable = true)
        BigDecimal puntajeObtenido,
        DatosAdicionalesDTO datosAdicionales,
        List<CompetenciaNoAlcanzadaDTO> competenciasNoAlcanzadas
) {
    public FilaNoAlcanzoMinimoDTO {
        competenciasNoAlcanzadas = competenciasNoAlcanzadas != null
                ? List.copyOf(competenciasNoAlcanzadas)
                : List.of();
    }
}
