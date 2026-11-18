package ar.edu.utn.frsf.g9.capitalhumano.features.auth.exception;

import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.CapitalHumanoException;

/**
 * Excepción lanzada cuando fallan las credenciales o validaciones de autenticación en el sistema.
 */
public class AutenticacionException extends CapitalHumanoException {

    public static final String CODIGO = "AUTENTICACION_FALLIDA";

    public AutenticacionException(String mensaje) {
        super(CODIGO, mensaje);
    }
}
