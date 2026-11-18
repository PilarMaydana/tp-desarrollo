package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implementación de {@link ILdapAuthStrategy} para entorno de producción.
 *
 * <p>Preparada para interactuar directamente con el servidor de directorio LDAP institucional.</p>
 */
@Component
@Profile("prod")
public class RealLdapAuthStrategy implements ILdapAuthStrategy {

    private static final Logger log = LoggerFactory.getLogger(RealLdapAuthStrategy.class);

    @Override
    public boolean authenticate(String usuario, String contrasena) {
        log.info("Iniciando autenticación real contra LDAP corporativo para usuario '{}'", usuario);
        // En producción se realiza el bind contra el directorio LDAP corporativo configurado.
        // Por seguridad y contingencia inicial, si no está configurado el servidor se retorna false.
        if (usuario == null || contrasena == null || usuario.isBlank() || contrasena.isBlank()) {
            return false;
        }

        // Stub extensible con Spring Security LDAP / LdapTemplate
        return false;
    }
}
