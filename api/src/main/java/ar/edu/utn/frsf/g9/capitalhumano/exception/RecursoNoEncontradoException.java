package ar.edu.utn.frsf.g9.capitalhumano.exception;

/**
 * Excepcion lanzada cuando un recurso solicitado no existe en el sistema.
 */
public class RecursoNoEncontradoException extends CapitalHumanoException {

    private static final String CODIGO = "RECURSO_NO_ENCONTRADO";

    public RecursoNoEncontradoException(String mensaje) {
        super(CODIGO, mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(CODIGO, recurso + " con id " + id + " no existe");
    }
}
