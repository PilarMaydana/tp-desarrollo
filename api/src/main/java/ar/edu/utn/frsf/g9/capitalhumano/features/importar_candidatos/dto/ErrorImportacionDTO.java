package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Estructura de respuesta devuelta ante fallos de formato o validación en el archivo CSV (HTTP 400 Bad Request).
 */
@Schema(description = "Detalle de errores detectados en la validación o parseo del archivo CSV")
public record ErrorImportacionDTO(
        @Schema(description = "Mensaje global descriptivo del fallo", example = "El archivo CSV contiene errores de formato o validación.")
        String mensaje,

        @Schema(description = "Listado de detalles de error específicos encontrados en el archivo")
        List<DetalleErrorFilaDTO> errores
) {

    public ErrorImportacionDTO {
        errores = errores != null ? List.copyOf(errores) : List.of();
    }

    /**
     * Detalle específico de un error detectado en una fila o a nivel de archivo.
     */
    @Schema(description = "Detalle específico de error en una fila o columna del archivo")
    public record DetalleErrorFilaDTO(
            @Schema(description = "Número de fila en el archivo CSV (1-based, null si es a nivel de archivo completo)", example = "2")
            Integer fila,

            @Schema(description = "Nombre de la columna o campo causante del error", example = "Tipo documento")
            String campo,

            @Schema(description = "Descripción legible del error", example = "Valor inválido: 'PASAPORTE'. Valores permitidos: DNI, LE, LC, PP.")
            String mensaje
    ) {
    }
}
