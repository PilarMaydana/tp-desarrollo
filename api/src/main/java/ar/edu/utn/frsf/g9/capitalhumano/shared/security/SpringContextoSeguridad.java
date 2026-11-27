package ar.edu.utn.frsf.g9.capitalhumano.shared.security;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Implementación real de {@link ContextoSeguridad} respaldada por Spring Security y el JWT
 * autenticado en {@link SecurityContextHolder}.
 *
 * <p>Cuando no existe un usuario autenticado en el contexto (por ejemplo, en pruebas de integración
 * que invocan gestores directamente sin capa HTTP o llamadas de desarrollo sin token), aplica
 * fallback al ID {@code 1L} para mantener compatibilidad.</p>
 */
@Component
@Primary
public class SpringContextoSeguridad implements ContextoSeguridad {

    private static final Long DEFAULT_FALLBACK_ID = 1L;

    @Override
    public Long getUsuarioActualId() {
        UsuarioAutenticado usuario = obtenerPrincipalAutenticado();
        if (usuario != null && usuario.usuarioId() != null) {
            return usuario.usuarioId();
        }
        return DEFAULT_FALLBACK_ID;
    }

    @Override
    public Long obtenerCandidatoActual() {
        UsuarioAutenticado usuario = obtenerPrincipalAutenticado();
        if (usuario != null) {
            if ("CANDIDATO".equalsIgnoreCase(usuario.rol())) {
                return usuario.usuarioId();
            }
            return null;
        }
        return DEFAULT_FALLBACK_ID;
    }

    private UsuarioAutenticado obtenerPrincipalAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        return null;
    }
}
