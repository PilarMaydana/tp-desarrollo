package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.exception;

import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.ErrorImportacionDTO;

import java.util.List;

/**
 * Excepción lanzada cuando la estructura o el contenido del archivo CSV no cumple las reglas de importación.
 */
public class CsvImportacionException extends RuntimeException {

    private final List<ErrorImportacionDTO.DetalleErrorFilaDTO> errores;

    public CsvImportacionException(String mensaje) {
        super(mensaje);
        this.errores = List.of(new ErrorImportacionDTO.DetalleErrorFilaDTO(null, null, mensaje));
    }

    public CsvImportacionException(String mensaje, List<ErrorImportacionDTO.DetalleErrorFilaDTO> errores) {
        super(mensaje);
        this.errores = errores != null ? List.copyOf(errores) : List.of();
    }

    public List<ErrorImportacionDTO.DetalleErrorFilaDTO> getErrores() {
        return errores;
    }

    public ErrorImportacionDTO toDTO() {
        return new ErrorImportacionDTO(getMessage(), errores);
    }
}
