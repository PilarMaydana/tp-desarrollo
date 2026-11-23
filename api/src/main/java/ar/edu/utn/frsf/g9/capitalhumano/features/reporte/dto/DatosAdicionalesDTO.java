package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Datos adicionales de un cuestionario según la Observación (2) del CU 028.
 *
 * <p>Convención consistente de campos nulos:</p>
 * <ul>
 *     <li><b>Cuestionarios COMPLETO:</b> se informan {@code fechaInicio}, {@code fechaFin} y
 *         {@code cantidadAccesos}; {@code ultimoIngreso} se envía siempre como {@code null}.</li>
 *     <li><b>Cuestionarios NO completos</b> (INCOMPLETO, EN_PROCESO, SIN_CONTESTAR, ACTIVO):
 *         se informan {@code fechaInicio}, {@code ultimoIngreso} (pueden ser {@code null} si el
 *         cuestionario nunca se inició) y {@code cantidadAccesos}; {@code fechaFin} se envía siempre
 *         como {@code null}.</li>
 * </ul>
 */
@Schema(description = "Datos adicionales de cursado del cuestionario (Observación 2 del CU 028)")
public record DatosAdicionalesDTO(
        @Schema(description = "Fecha y hora de inicio del cuestionario (null si nunca se inició)", nullable = true)
        LocalDateTime fechaInicio,
        @Schema(description = "Fecha y hora de finalización (solo para cuestionarios COMPLETO; null en el resto)", nullable = true)
        LocalDateTime fechaFin,
        @Schema(description = "Fecha y hora del último ingreso (solo para cuestionarios no completos; null en COMPLETO o si nunca ingresó)", nullable = true)
        LocalDateTime ultimoIngreso,
        @Schema(description = "Cantidad total de accesos consumidos por el candidato")
        int cantidadAccesos
) {

    public static DatosAdicionalesDTO paraCompleto(LocalDateTime fechaInicio,
                                                   LocalDateTime fechaFin,
                                                   int cantidadAccesos) {
        return new DatosAdicionalesDTO(fechaInicio, fechaFin, null, cantidadAccesos);
    }

    public static DatosAdicionalesDTO paraNoCompleto(LocalDateTime fechaInicio,
                                                     LocalDateTime ultimoIngreso,
                                                     int cantidadAccesos) {
        return new DatosAdicionalesDTO(fechaInicio, null, ultimoIngreso, cantidadAccesos);
    }
}
