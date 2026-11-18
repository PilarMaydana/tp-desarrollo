package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dao.CandidatoRepository;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dao.CuestionarioRepository;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.AuthResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.CandidatoLoginRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.ConsultorLoginRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.exception.AutenticacionException;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ILdapAuthStrategy ldapAuthStrategy;

    @Mock
    private CandidatoRepository candidatoRepository;

    @Mock
    private CuestionarioRepository cuestionarioRepository;

    private JwtTokenService jwtTokenService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService("test-secret-key-that-is-at-least-32-bytes-long", 3600, new ObjectMapper());
        authService = new AuthService(ldapAuthStrategy, candidatoRepository, cuestionarioRepository, jwtTokenService);
    }

    @Nested
    @DisplayName("Autenticación de Consultores (LDAP)")
    class ConsultorAuthTests {

        @Test
        @DisplayName("Autenticación exitosa retorna token, ID de usuario y rol CONSULTOR")
        void autenticarConsultor_exitoso() {
            ConsultorLoginRequestDTO request = new ConsultorLoginRequestDTO("ADMIN", "admin123");
            when(ldapAuthStrategy.authenticate("ADMIN", "admin123")).thenReturn(true);

            AuthResponseDTO response = authService.autenticarConsultor(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isNotBlank();
            assertThat(response.getRol()).isEqualTo("CONSULTOR");
            assertThat(response.getUsuarioId()).isEqualTo(1L);
            assertThat(response.getCuestionarioId()).isNull();
            verify(ldapAuthStrategy).authenticate("ADMIN", "admin123");
        }

        @Test
        @DisplayName("Requerimiento especial: El usuario ingresado en minúsculas se normaliza a mayúsculas")
        void autenticarConsultor_convierteMayusculas() {
            ConsultorLoginRequestDTO request = new ConsultorLoginRequestDTO("lfernandez", "admin123");
            when(ldapAuthStrategy.authenticate("LFERNANDEZ", "admin123")).thenReturn(true);

            AuthResponseDTO response = authService.autenticarConsultor(request);

            assertThat(response).isNotNull();
            assertThat(response.getRol()).isEqualTo("CONSULTOR");
            verify(ldapAuthStrategy).authenticate("LFERNANDEZ", "admin123");
        }

        @Test
        @DisplayName("Flujo alternativo 5.A: Credenciales inválidas lanzan AutenticacionException con mensaje exacto")
        void autenticarConsultor_credencialesInvalidas_lanzaExcepcion() {
            ConsultorLoginRequestDTO request = new ConsultorLoginRequestDTO("ADMIN", "wrongpass");
            when(ldapAuthStrategy.authenticate("ADMIN", "wrongpass")).thenReturn(false);

            assertThatThrownBy(() -> authService.autenticarConsultor(request))
                    .isInstanceOf(AutenticacionException.class)
                    .hasMessage("El usuario o la contraseña no son válidos");
        }
    }

    @Nested
    @DisplayName("Autenticación de Candidatos (Cuestionario)")
    class CandidatoAuthTests {

        private Candidato candidatoMock;
        private Cuestionario cuestionarioMock;

        @BeforeEach
        void initData() {
            candidatoMock = new Candidato(10L, 100L, TipoDocumento.DNI, "12345678", "Pérez", "Juan", "juan@example.com");
            cuestionarioMock = new Cuestionario(50L, candidatoMock, EstadoCuestionario.ACTIVO, "CLAVE123");
        }

        @Test
        @DisplayName("Autenticación exitosa con cuestionario ACTIVO retorna token, ID de cuestionario y rol CANDIDATO")
        void autenticarCandidato_exitosoCuestionarioActivo() {
            CandidatoLoginRequestDTO request = new CandidatoLoginRequestDTO(TipoDocumento.DNI, "12345678", "CLAVE123");

            when(candidatoRepository.findByTipoDocumentoAndNroDocumento(TipoDocumento.DNI, "12345678"))
                    .thenReturn(Optional.of(candidatoMock));
            when(cuestionarioRepository.findByCandidatoAndEstadoInAndClaveAcceso(
                    eq(candidatoMock),
                    eq(List.of(EstadoCuestionario.ACTIVO, EstadoCuestionario.EN_PROCESO)),
                    eq("CLAVE123")
            )).thenReturn(Optional.of(cuestionarioMock));

            AuthResponseDTO response = authService.autenticarCandidato(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isNotBlank();
            assertThat(response.getRol()).isEqualTo("CANDIDATO");
            assertThat(response.getUsuarioId()).isEqualTo(10L);
            assertThat(response.getCuestionarioId()).isEqualTo(50L);
        }

        @Test
        @DisplayName("Autenticación exitosa con cuestionario EN_PROCESO retorna token y rol CANDIDATO")
        void autenticarCandidato_exitosoCuestionarioEnProceso() {
            cuestionarioMock.setEstado(EstadoCuestionario.EN_PROCESO);
            CandidatoLoginRequestDTO request = new CandidatoLoginRequestDTO(TipoDocumento.DNI, "12345678", "CLAVE123");

            when(candidatoRepository.findByTipoDocumentoAndNroDocumento(TipoDocumento.DNI, "12345678"))
                    .thenReturn(Optional.of(candidatoMock));
            when(cuestionarioRepository.findByCandidatoAndEstadoInAndClaveAcceso(
                    any(Candidato.class), any(), eq("CLAVE123")
            )).thenReturn(Optional.of(cuestionarioMock));

            AuthResponseDTO response = authService.autenticarCandidato(request);

            assertThat(response).isNotNull();
            assertThat(response.getRol()).isEqualTo("CANDIDATO");
            assertThat(response.getCuestionarioId()).isEqualTo(50L);
        }

        @Test
        @DisplayName("Flujo alternativo 3.A: Candidato no encontrado lanza AutenticacionException con mensaje exacto")
        void autenticarCandidato_noExiste_lanzaExcepcion() {
            CandidatoLoginRequestDTO request = new CandidatoLoginRequestDTO(TipoDocumento.DNI, "99999999", "CLAVE123");

            when(candidatoRepository.findByTipoDocumentoAndNroDocumento(TipoDocumento.DNI, "99999999"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.autenticarCandidato(request))
                    .isInstanceOf(AutenticacionException.class)
                    .hasMessage("Los datos ingresados no son válidos o no existe un cuestionario para el Candidato");
        }

        @Test
        @DisplayName("Flujo alternativo 3.A: Clave incorrecta o cuestionario no activo/en proceso lanza excepción idéntica")
        void autenticarCandidato_claveIncorrecta_lanzaExcepcion() {
            CandidatoLoginRequestDTO request = new CandidatoLoginRequestDTO(TipoDocumento.DNI, "12345678", "WRONGKEY");

            when(candidatoRepository.findByTipoDocumentoAndNroDocumento(TipoDocumento.DNI, "12345678"))
                    .thenReturn(Optional.of(candidatoMock));
            when(cuestionarioRepository.findByCandidatoAndEstadoInAndClaveAcceso(
                    any(Candidato.class), any(), eq("WRONGKEY")
            )).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.autenticarCandidato(request))
                    .isInstanceOf(AutenticacionException.class)
                    .hasMessage("Los datos ingresados no son válidos o no existe un cuestionario para el Candidato");
        }
    }
}
