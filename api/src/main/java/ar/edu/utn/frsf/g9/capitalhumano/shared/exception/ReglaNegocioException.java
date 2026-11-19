package ar.edu.utn.frsf.g9.capitalhumano.shared.exception;

import java.util.List;

/**
 * Excepcion lanzada cuando una accion viola una regla o invariante de negocio.
 */
public class ReglaNegocioException extends CapitalHumanoException {

    private static final String CODIGO = "REGLA_NEGOCIO";

    private final List<DetalleError> detalles;

    /**
     * Detalle individual de una violación de regla de negocio.
     *
     * @param campo   identificador o categoría del elemento afectado
     * @param mensaje descripción detallada del incumplimiento
     */
    public record DetalleError(String campo, String mensaje) {
    }

    public ReglaNegocioException(String mensaje) {
        this(mensaje, List.of());
    }

    public ReglaNegocioException(String mensaje, List<DetalleError> detalles) {
        super(CODIGO, mensaje);
        this.detalles = detalles != null ? List.copyOf(detalles) : List.of();
    }

    public List<DetalleError> getDetalles() {
        return detalles;
    }
}
