package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.ResultadoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de integración HTTP E2E que valida el flujo completo con JWT real:
 * CU 025 (asignar cuestionario a un candidato con ID != 1) -> CU 001 (login candidato -> JWT)
 * -> CU 026 (instrucciones, iniciar, responder todos los bloques y aislamiento entre candidatos).
 */
@SpringBootTest
class CuestionarioJwtFlowIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EvaluacionDAO evaluacionDAO;

    @Autowired
    private CuestionarioDAO cuestionarioDAO;

    @Autowired
    private ResultadoDAO resultadoDAO;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("Candidato distinto de ID 1 puede autenticarse con JWT y completar su cuestionario asignado")
    void candidatoDistintoDeUno_autenticaConJwtYCompletaCuestionario() throws Exception {
        Long evaluacionId = null;
        Long cuestionarioId = null;

        try {
            // 1. CU 025: Asignar cuestionario al candidato ID 4 (ANA FERNÁNDEZ, DNI 30456789) para puesto ID 1
            MvcResult evalResult = mockMvc.perform(post("/api/evaluaciones")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "puestoId": 1,
                                      "candidatoIds": [4]
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.cuestionarios[0].candidatoId").value(4))
                    .andReturn();

            JsonNode evalJson = objectMapper.readTree(evalResult.getResponse().getContentAsString());
            evaluacionId = evalJson.get("evaluacionId").asLong();
            JsonNode cuestionarioNode = evalJson.get("cuestionarios").get(0);
            cuestionarioId = cuestionarioNode.get("cuestionarioId").asLong();
            String claveAcceso = cuestionarioNode.get("claveAcceso").asText();

            // 2. CU 001: Login del candidato ID 4 -> obtiene JWT real
            MvcResult loginResult = mockMvc.perform(post("/api/auth/candidato")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "tipoDocumento", "DNI",
                                    "numeroDocumento", "30456789",
                                    "clave", claveAcceso
                            ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rol").value("CANDIDATO"))
                    .andExpect(jsonPath("$.usuarioId").value(4))
                    .andExpect(jsonPath("$.cuestionarioId").value(cuestionarioId))
                    .andReturn();

            String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                    .get("token").asText();
            assertThat(token).isNotBlank();

            // 3. Aislamiento: candidato 4 NO puede acceder al cuestionario ID 6 (de candidato 1)
            mockMvc.perform(get("/api/cuestionarios/6/instrucciones")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());

            // 4. CU 026 Paso 1: Obtener instrucciones del cuestionario asignado al candidato 4
            mockMvc.perform(get("/api/cuestionarios/{id}/instrucciones", cuestionarioId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(cuestionarioId))
                    .andExpect(jsonPath("$.estado").value("ACTIVO"))
                    .andExpect(jsonPath("$.nombrePuesto").value("DESARROLLADOR SENIOR"));

            // 5. CU 026 Paso 2: Iniciar el cuestionario
            MvcResult initResult = mockMvc.perform(post("/api/cuestionarios/{id}/iniciar", cuestionarioId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("EN_PROCESO"))
                    .andExpect(jsonPath("$.bloqueActual.numero").value(1))
                    .andReturn();

            JsonNode bloqueActual = objectMapper.readTree(initResult.getResponse().getContentAsString())
                    .get("bloqueActual");

            // 6. CU 026 Pasos 5-8: Responder todos los bloques hasta completar
            boolean completado = false;
            int safetyCounter = 0;
            while (!completado && safetyCounter++ < 10) {
                int numeroBloque = bloqueActual.get("numero").asInt();
                List<Map<String, Object>> respuestas = new ArrayList<>();
                for (JsonNode pregunta : bloqueActual.get("preguntas")) {
                    assertThat(pregunta.get("opciones").size()).isGreaterThanOrEqualTo(2);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("preguntaAsignadaId", pregunta.get("preguntaAsignadaId").asLong());
                    item.put("opcionId", pregunta.get("opciones").get(0).get("id").asLong());
                    respuestas.add(item);
                }

                MvcResult respResult = mockMvc.perform(
                                post("/api/cuestionarios/{id}/bloques/{numero}/respuestas", cuestionarioId, numeroBloque)
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(Map.of("respuestas", respuestas))))
                        .andExpect(status().isOk())
                        .andReturn();

                JsonNode respJson = objectMapper.readTree(respResult.getResponse().getContentAsString());
                completado = respJson.get("completado").asBoolean();
                if (!completado) {
                    bloqueActual = respJson.get("bloqueSiguiente");
                }
            }

            assertThat(completado).isTrue();
            assertThat(cuestionarioDAO.findById(cuestionarioId).orElseThrow().getEstado().name())
                    .isEqualTo("COMPLETO");
        } finally {
            Long finalCuestionarioId = cuestionarioId;
            Long finalEvaluacionId = evaluacionId;
            transactionTemplate.executeWithoutResult(status -> {
                if (finalCuestionarioId != null) {
                    resultadoDAO.findByCuestionarioId(finalCuestionarioId).ifPresent(r -> {
                        resultadoDAO.delete(r);
                        resultadoDAO.flush();
                    });
                }
                if (finalEvaluacionId != null) {
                    evaluacionDAO.findById(finalEvaluacionId).ifPresent(e -> {
                        evaluacionDAO.delete(e);
                        evaluacionDAO.flush();
                    });
                }
            });
        }
    }
}
