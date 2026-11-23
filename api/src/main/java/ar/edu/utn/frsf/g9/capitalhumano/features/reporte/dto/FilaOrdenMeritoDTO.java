package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Fila del grupo principal {@code ordenDeMerito}: cuestionario COMPLETO que alcanzó el mínimo
 * en todas las competencias del puesto.
 */
@Schema(description = "Candidato (cuestionario) que integra el orden de mérito principal")
public record FilaOrdenMeritoDTO(
        int posicion,
        Long cuestionarioId,
        Long evaluacionId,
        TipoDocumento tipoDocumento,
        String nroDocumento,
        String apellido,
        String nombre,
        BigDecimal puntajeObtenido,
        DatosAdicionalesDTO datosAdicionales
) {
}
