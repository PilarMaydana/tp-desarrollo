package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Respuesta de previsualización de la evaluación antes de confirmar (CU 025 - Paso 11).
 * No persiste ningún registro en base de datos para soportar cancelación (Flujo 12.A).
 */
@Schema(description = "Resumen de previsualización de evaluación previo a la confirmación final")
public record PrevisualizacionEvaluacionDTO(
        @Schema(description = "Identificador del puesto", example = "1")
        Long puestoId,

        @Schema(description = "Código del puesto", example = "DEV-01")
        String puestoCodigo,

        @Schema(description = "Nombre del puesto o función", example = "DESARROLLADOR SENIOR")
        String puestoNombre,

        @Schema(description = "Empresa solicitante", example = "TECH SOLUTIONS")
        String empresa,

        @Schema(description = "Tiempo máximo configurado para los cuestionarios en minutos", example = "60")
        Integer tiempoMaximoMinutos,

        @Schema(description = "Cantidad de preguntas por bloque configurada", example = "5")
        Integer cantidadPreguntasPorBloque,

        @Schema(description = "Lista de candidatos a evaluar con sus claves de ingreso sugeridas")
        List<CandidatoClaveDTO> candidatos
) {
}
