package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

/**
 * Estrategia de autenticación para usuarios consultores frente al directorio LDAP institucional.
 */
public interface ILdapAuthStrategy {

    /**
     * Valida las credenciales de un usuario consultor en el directorio LDAP.
     *
     * @param usuario identificador de usuario (en mayúsculas)
     * @param contrasena contraseña provista
     * @return {@code true} si las credenciales son válidas, {@code false} en caso contrario
     */
    boolean authenticate(String usuario, String contrasena);
}
