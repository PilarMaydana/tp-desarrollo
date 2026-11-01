package ar.edu.utn.frsf.g9.capitalhumano.shared.security;

/**
 * Interfaz que provee acceso al contexto de seguridad y al usuario actualmente autenticado.
 */
public interface ContextoSeguridad {

    /**
     * Obtiene el identificador del usuario actualmente autenticado en el sistema.
     *
     * @return ID numérico del usuario actual
     */
    Long getUsuarioActualId();
}

