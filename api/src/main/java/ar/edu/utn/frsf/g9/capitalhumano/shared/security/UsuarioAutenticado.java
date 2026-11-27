package ar.edu.utn.frsf.g9.capitalhumano.shared.security;

/**
 * Principal autenticado extraído de un token JWT válido.
 */
public record UsuarioAutenticado(
        Long usuarioId,
        String rol,
        String subject,
        Long cuestionarioId
) {
}
