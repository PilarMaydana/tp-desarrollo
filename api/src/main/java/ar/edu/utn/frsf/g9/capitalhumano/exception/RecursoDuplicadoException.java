package ar.edu.utn.frsf.g9.capitalhumano.exception;

/**
 * Excepcion lanzada cuando se intenta crear o registrar un recurso que ya existe
 * o que viola una restriccion de unicidad.
 */
public class RecursoDuplicadoException extends CapitalHumanoException {

    private static final String CODIGO = "RECURSO_DUPLICADO";

    public RecursoDuplicadoException(String mensaje) {
        super(CODIGO, mensaje);
    }
}
