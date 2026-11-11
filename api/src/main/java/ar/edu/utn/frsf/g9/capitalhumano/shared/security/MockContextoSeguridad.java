package ar.edu.utn.frsf.g9.capitalhumano.shared.security;

import org.springframework.stereotype.Component;

/**
 * Implementación mock del {@link ContextoSeguridad} para desarrollo y pruebas.
 * Retorna de forma predeterminada el ID 1 correspondiente a un Consultor genérico.
 *
 * <p><b>TODO:</b> Reemplazar por la implementación real conectada a Spring Security / LDAP al desarrollar el CU 001.</p>
 */
@Component
public class MockContextoSeguridad implements ContextoSeguridad {

    @Override
    public Long getUsuarioActualId() {
        return 1L;
    }
}

