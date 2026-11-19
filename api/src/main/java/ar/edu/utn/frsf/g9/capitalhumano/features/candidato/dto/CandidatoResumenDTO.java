package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resumen de un candidato para búsqueda y selección en el CU 025.
 */
@Schema(description = "Datos resumidos de un candidato activo")
public record CandidatoResumenDTO(
        @Schema(description = "Identificador único del candidato", example = "4")
        Long id,

        @Schema(description = "Número de candidato", example = "4")
        String nroCandidato,

        @Schema(description = "Apellido del candidato", example = "FERNÁNDEZ")
        String apellido,

        @Schema(description = "Nombre del candidato", example = "ANA")
        String nombre,

        @Schema(description = "Tipo de documento de identidad", example = "DNI")
        TipoDocumento tipoDocumento,

        @Schema(description = "Número de documento de identidad", example = "30456789")
        String nroDocumento,

        @Schema(description = "Correo electrónico del candidato", example = "ana.fernandez@example.com")
        String email
) {
}
