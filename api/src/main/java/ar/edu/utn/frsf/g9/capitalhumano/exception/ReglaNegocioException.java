package ar.edu.utn.frsf.g9.capitalhumano.exception;

/**
 * Excepcion lanzada cuando una accion viola una regla o invariante de negocio.
 */
public class ReglaNegocioException extends CapitalHumanoException {

    private static final String CODIGO = "REGLA_NEGOCIO";

    public ReglaNegocioException(String mensaje) {
        super(CODIGO, mensaje);
    }
}
