package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto.CandidatoResumenDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Resultado exitoso de la validación de candidatos seleccionados en el CU 025 (Paso 6).
 */
@Schema(description = "Resultado de validación de candidatos aptos para evaluación")
public record ValidacionCandidatosResponseDTO(
        @Schema(description = "Indica si todos los candidatos seleccionados están habilitados para ser evaluados", example = "true")
        boolean valido,

        @Schema(description = "Cantidad total de candidatos validados", example = "2")
        int cantidadCandidatos,

        @Schema(description = "Lista de candidatos validados")
        List<CandidatoResumenDTO> candidatos
) {
}
