package ar.edu.utn.frsf.g9.capitalhumano.shared.exception;

import java.util.List;

/**
 * Excepcion lanzada desde los gestores cuando fallan validaciones de entrada o completitud
 * que deben reportarse con HTTP 400 y codigo {@code VALIDACION} junto a sus detalles por campo.
 */
public class ValidacionException extends CapitalHumanoException {

    private static final String CODIGO = "VALIDACION";

    private final List<DetalleError> detalles;

    /**
     * Detalle individual de un error de validacion.
     *
     * @param campo   identificador del campo o elemento faltante/invalido
     * @param mensaje descripcion legible del motivo de la falla
     */
    public record DetalleError(String campo, String mensaje) {
    }

    public ValidacionException(String mensaje) {
        this(mensaje, List.of());
    }

    public ValidacionException(String mensaje, List<DetalleError> detalles) {
        super(CODIGO, mensaje);
        this.detalles = detalles != null ? List.copyOf(detalles) : List.of();
    }

    public List<DetalleError> getDetalles() {
        return detalles;
    }
}
