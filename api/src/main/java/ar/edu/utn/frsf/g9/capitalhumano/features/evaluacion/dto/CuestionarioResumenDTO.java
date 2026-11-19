package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resumen de un cuestionario generado y persistido para un candidato dentro de una evaluación.
 */
@Schema(description = "Resumen de un cuestionario generado en estado ACTIVO")
public record CuestionarioResumenDTO(
        @Schema(description = "Identificador único del cuestionario", example = "6")
        Long cuestionarioId,

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

        @Schema(description = "Clave de acceso generada", example = "A7K9M2P4")
        String claveAcceso,

        @Schema(description = "Estado inicial del cuestionario", example = "ACTIVO")
        EstadoCuestionario estado,

        @Schema(description = "Cantidad de bloques generados en el cuestionario", example = "2")
        int cantidadBloques,

        @Schema(description = "Cantidad total de preguntas asignadas en el cuestionario", example = "6")
        int cantidadPreguntas
) {
}
