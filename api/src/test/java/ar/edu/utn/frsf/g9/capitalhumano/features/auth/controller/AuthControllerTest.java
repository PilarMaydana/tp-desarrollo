package ar.edu.utn.frsf.g9.capitalhumano.features.auth.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.AuthResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.CandidatoLoginRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.ConsultorLoginRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.exception.AutenticacionException;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.service.AuthService;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("Endpoint POST /api/auth/consultor")
    class ConsultorEndpointTests {

        @Test
        @DisplayName("POST /api/auth/consultor exitoso retorna HTTP 200 y AuthResponseDTO")
        void autenticarConsultor_exitoso() throws Exception {
            AuthResponseDTO dto = AuthResponseDTO.paraConsultor("mock-jwt-token-consultor", 1L, "Lucía", "Fernández");
            when(authService.autenticarConsultor(any(ConsultorLoginRequestDTO.class))).thenReturn(dto);

            String requestBody = """
                    {
                      "usuario": "LFERNANDEZ",
                      "contrasena": "admin123"
                    }
                    """;

            mockMvc.perform(post("/api/auth/consultor")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("mock-jwt-token-consultor"))
                    .andExpect(jsonPath("$.rol").value("CONSULTOR"))
                    .andExpect(jsonPath("$.usuarioId").value(1L))
                    .andExpect(jsonPath("$.nombre").value("Lucía"))
                    .andExpect(jsonPath("$.apellido").value("Fernández"));
        }

        @Test
        @DisplayName("POST /api/auth/consultor con credenciales inválidas retorna HTTP 401 y mensaje de negocio exacto")
        void autenticarConsultor_invalido_retorna401() throws Exception {
            when(authService.autenticarConsultor(any(ConsultorLoginRequestDTO.class)))
                    .thenThrow(new AutenticacionException("El usuario o la contraseña no son válidos"));

            String requestBody = """
                    {
                      "usuario": "ADMIN",
                      "contrasena": "wrong"
                    }
                    """;

            mockMvc.perform(post("/api/auth/consultor")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.codigo").value("AUTENTICACION_FALLIDA"))
                    .andExpect(jsonPath("$.mensaje").value("El usuario o la contraseña no son válidos"));
        }

        @Test
        @DisplayName("POST /api/auth/consultor con datos incompletos retorna HTTP 400 Bad Request")
        void autenticarConsultor_validacion_retorna400() throws Exception {
            String requestBody = """
                    {
                      "usuario": "",
                      "contrasena": ""
                    }
                    """;

            mockMvc.perform(post("/api/auth/consultor")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.codigo").value("VALIDACION"));
        }
    }

    @Nested
    @DisplayName("Endpoint POST /api/auth/candidato")
    class CandidatoEndpointTests {

        @Test
        @DisplayName("POST /api/auth/candidato exitoso retorna HTTP 200 y AuthResponseDTO")
        void autenticarCandidato_exitoso() throws Exception {
            AuthResponseDTO dto = AuthResponseDTO.paraCandidato("mock-jwt-token-candidato", 5L, 25L);
            when(authService.autenticarCandidato(any(CandidatoLoginRequestDTO.class))).thenReturn(dto);

            String requestBody = """
                    {
                      "tipoDocumento": "DNI",
                      "numeroDocumento": "12345678",
                      "clave": "CLAVE123"
                    }
                    """;

            mockMvc.perform(post("/api/auth/candidato")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("mock-jwt-token-candidato"))
                    .andExpect(jsonPath("$.rol").value("CANDIDATO"))
                    .andExpect(jsonPath("$.usuarioId").value(5L))
                    .andExpect(jsonPath("$.cuestionarioId").value(25L));
        }

        @Test
        @DisplayName("POST /api/auth/candidato con datos erróneos retorna HTTP 401 y mensaje de negocio exacto")
        void autenticarCandidato_invalido_retorna401() throws Exception {
            when(authService.autenticarCandidato(any(CandidatoLoginRequestDTO.class)))
                    .thenThrow(new AutenticacionException("Los datos ingresados no son válidos o no existe un cuestionario para el Candidato"));

            String requestBody = """
                    {
                      "tipoDocumento": "DNI",
                      "numeroDocumento": "12345678",
                      "clave": "ERRONEA"
                    }
                    """;

            mockMvc.perform(post("/api/auth/candidato")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.codigo").value("AUTENTICACION_FALLIDA"))
                    .andExpect(jsonPath("$.mensaje").value("Los datos ingresados no son válidos o no existe un cuestionario para el Candidato"));
        }

        @Test
        @DisplayName("POST /api/auth/candidato con clave mayor a 8 caracteres retorna HTTP 400 Bad Request")
        void autenticarCandidato_claveMayorA8_retorna400() throws Exception {
            String requestBody = """
                    {
                      "tipoDocumento": "DNI",
                      "numeroDocumento": "12345678",
                      "clave": "123456789"
                    }
                    """;

            mockMvc.perform(post("/api/auth/candidato")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.codigo").value("VALIDACION"));
        }
    }
}
