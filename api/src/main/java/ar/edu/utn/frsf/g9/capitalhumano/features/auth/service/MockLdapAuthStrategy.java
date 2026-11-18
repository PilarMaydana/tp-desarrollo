package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implementación simulada (Mock/Stub) de {@link ILdapAuthStrategy} para desarrollo local y testing.
 *
 * <p>Permite la autenticación con credenciales predefinidas y controladas sin requerir
 * la conexión a un servidor LDAP corporativo activo.</p>
 */
@Component
@Profile("!prod")
public class MockLdapAuthStrategy implements ILdapAuthStrategy {

    private static final Logger log = LoggerFactory.getLogger(MockLdapAuthStrategy.class);

    /**
     * Diccionario de credenciales predefinidas (USUARIO_MAYUSCULAS -> CONTRASEÑA).
     */
    private static final Map<String, String> CREDENCIALES_VALIDAS = Map.of(
            "ADMIN", "admin123",
            "CONSULTOR", "consultor123",
            "LFERNANDEZ", "admin123",
            "USER_TEST", "pass123"
    );

    @Override
    public boolean authenticate(String usuario, String contrasena) {
        if (usuario == null || contrasena == null) {
            return false;
        }

        String usuarioUpper = usuario.trim().toUpperCase();
        String expectedPassword = CREDENCIALES_VALIDAS.get(usuarioUpper);

        boolean success = expectedPassword != null && expectedPassword.equals(contrasena);
        if (success) {
            log.info("Autenticación LDAP simulada exitosa para usuario '{}'", usuarioUpper);
        } else {
            log.warn("Autenticación LDAP simulada fallida para usuario '{}'", usuarioUpper);
        }

        return success;
    }
}
