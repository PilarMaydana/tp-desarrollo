package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Datos del candidato junto con la clave de ingreso generada para la pantalla de confirmación (CU 025 - Paso 11).
 */
@Schema(description = "Candidato a evaluar con su clave de ingreso generada")
public record CandidatoClaveDTO(
        @Schema(description = "Identificador único del candidato", example = "4")
        Long candidatoId,

        @Schema(description = "Número de candidato", example = "4")
        String nroCandidato,

        @Schema(description = "Apellido del candidato", example = "FERNÁNDEZ")
        String apellido,

        @Schema(description = "Nombre del candidato", example = "ANA")
        String nombre,

        @Schema(description = "Tipo de documento", example = "DNI")
        TipoDocumento tipoDocumento,

        @Schema(description = "Número de documento", example = "30456789")
        String nroDocumento,

        @Schema(description = "Clave aleatoria alfanumérica de 8 caracteres en mayúsculas", example = "A7K9M2P4")
        String claveAcceso
) {
}
