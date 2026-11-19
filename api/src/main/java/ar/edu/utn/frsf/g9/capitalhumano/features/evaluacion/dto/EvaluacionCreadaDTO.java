package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta devuelta al confirmar y persistir una evaluación con sus cuestionarios (CU 025 - Paso 13).
 */
@Schema(description = "Evaluación persistida junto con los cuestionarios generados")
public record EvaluacionCreadaDTO(
        @Schema(description = "Identificador único de la evaluación creada", example = "2")
        Long evaluacionId,

        @Schema(description = "Identificador del puesto evaluado", example = "1")
        Long puestoId,

        @Schema(description = "Código del puesto", example = "DEV-01")
        String puestoCodigo,

        @Schema(description = "Nombre del puesto o función", example = "DESARROLLADOR SENIOR")
        String puestoNombre,

        @Schema(description = "Empresa solicitante", example = "TECH SOLUTIONS")
        String empresa,

        @Schema(description = "Fecha y hora de creación de la evaluación")
        LocalDateTime fechaCreacion,

        @Schema(description = "Tiempo máximo configurado para los cuestionarios en minutos", example = "60")
        Integer tiempoMaximoMinutos,

        @Schema(description = "Cantidad de preguntas por bloque configurada", example = "5")
        Integer cantidadPreguntasPorBloque,

        @Schema(description = "Cuestionarios generados en estado ACTIVO")
        List<CuestionarioResumenDTO> cuestionarios
) {
}
