package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dao.CandidatoRepository;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dao.CuestionarioRepository;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.AuthResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.CandidatoLoginRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.ConsultorLoginRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.exception.AutenticacionException;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio principal de autenticación que implementa las reglas de negocio para el CU001.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public static final String ERROR_CONSULTOR = "El usuario o la contraseña no son válidos";
    public static final String ERROR_CANDIDATO = "Los datos ingresados no son válidos o no existe un cuestionario para el Candidato";

    private final ILdapAuthStrategy ldapAuthStrategy;
    private final CandidatoRepository candidatoRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            ILdapAuthStrategy ldapAuthStrategy,
            CandidatoRepository candidatoRepository,
            CuestionarioRepository cuestionarioRepository,
            JwtTokenService jwtTokenService
    ) {
        this.ldapAuthStrategy = ldapAuthStrategy;
        this.candidatoRepository = candidatoRepository;
        this.cuestionarioRepository = cuestionarioRepository;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Autentica a un usuario con perfil Consultor contra el directorio LDAP.
     *
     * <p>El nombre de usuario se normaliza a mayúsculas conforme al requerimiento especial del CU.</p>
     *
     * @param request datos de ingreso de consultor
     * @return DTO con token JWT, ID de usuario, rol "CONSULTOR" y datos nominativos
     * @throws AutenticacionException si las credenciales son inválidas
     */
    public AuthResponseDTO autenticarConsultor(ConsultorLoginRequestDTO request) {
        if (request == null || request.getUsuario() == null || request.getContrasena() == null) {
            throw new AutenticacionException(ERROR_CONSULTOR);
        }

        // Requerimiento especial: conversión siempre a mayúsculas
        String usuarioNormalizado = request.getUsuario().trim().toUpperCase();

        boolean autenticado = ldapAuthStrategy.authenticate(usuarioNormalizado, request.getContrasena());
        if (!autenticado) {
            log.warn("Fallo de autenticación para consultor: {}", usuarioNormalizado);
            throw new AutenticacionException(ERROR_CONSULTOR);
        }

        Long usuarioId = 1L; // ID representativo del consultor
        String token = jwtTokenService.generarTokenConsultor(usuarioNormalizado, usuarioId);

        String nombre = "ADMIN".equals(usuarioNormalizado) ? "Administrador" : "Lucía";
        String apellido = "ADMIN".equals(usuarioNormalizado) ? "del Sistema" : "Fernández";

        log.info("Consultor '{}' autenticado correctamente con ID {}", usuarioNormalizado, usuarioId);
        return AuthResponseDTO.paraConsultor(token, usuarioId, nombre, apellido);
    }

    /**
     * Autentica a un candidato conforme a la especificación oficial del CU001 (Paso 3.A.3):
     * Comprueba que exista un candidato para el tipo y número de documento ingresados,
     * que tenga asociado un cuestionario ACTIVO o EN_PROCESO, y que la clave ingresada
     * coincida con la de dicho cuestionario.
     *
     * <p>Si el cuestionario ya está finalizado (COMPLETO) o no existe, la clave ya no es válida
     * y se rechaza el acceso con error 401.</p>
     *
     * @param request datos de ingreso de candidato (tipo doc, nro doc, clave)
     * @return DTO con token JWT, identificador del cuestionario activo, rol "CANDIDATO" y nombre/apellido
     * @throws AutenticacionException si los datos no son válidos o el cuestionario no está activo/en proceso
     */
    @Transactional(readOnly = true)
    public AuthResponseDTO autenticarCandidato(CandidatoLoginRequestDTO request) {
        if (request == null || request.getTipoDocumento() == null || request.getNumeroDocumento() == null || request.getClave() == null) {
            throw new AutenticacionException(ERROR_CANDIDATO);
        }

        String nroDocumento = request.getNumeroDocumento().trim();

        // 1. Consultar candidato por tipo y número de documento
        Candidato candidato = candidatoRepository
                .findByTipoDocumentoAndNroDocumento(request.getTipoDocumento(), nroDocumento)
                .orElseThrow(() -> {
                    log.warn("Candidato no encontrado para tipo {} y documento {}", request.getTipoDocumento(), nroDocumento);
                    return new AutenticacionException(ERROR_CANDIDATO);
                });

        // 2. Regla oficial CU001 (Paso 3.A.3): Solo cuestionarios ACTIVO o EN_PROCESO
        List<EstadoCuestionario> estadosValidos = List.of(EstadoCuestionario.ACTIVO, EstadoCuestionario.EN_PROCESO);

        Cuestionario cuestionario = cuestionarioRepository
                .findByCandidatoAndEstadoInAndClaveAcceso(candidato, estadosValidos, request.getClave().trim())
                .orElseThrow(() -> {
                    log.warn("No se encontró cuestionario ACTIVO o EN_PROCESO con la clave provista para el candidato ID {}", candidato.getId());
                    return new AutenticacionException(ERROR_CANDIDATO);
                });

        // 3. Generar token y responder
        String token = jwtTokenService.generarTokenCandidato(nroDocumento, candidato.getId(), cuestionario.getId());

        log.info("Candidato ID {} autenticado exitosamente para Cuestionario ID {}", candidato.getId(), cuestionario.getId());
        return AuthResponseDTO.paraCandidato(token, candidato.getId(), cuestionario.getId(), candidato.getNombre(), candidato.getApellido());
    }
}
