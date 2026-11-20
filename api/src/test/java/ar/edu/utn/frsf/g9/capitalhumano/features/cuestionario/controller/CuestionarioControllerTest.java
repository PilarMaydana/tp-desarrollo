package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.BloqueCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioIniciadoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.InstruccionesCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.OpcionPreguntaCandidatoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.PreguntaAsignadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests MockMvc para {@link CuestionarioController} cubriendo 200, 404 y 422 de cada endpoint (CU 026 Parte 1).
 */
@ExtendWith(MockitoExtension.class)
class CuestionarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestorCuestionario gestorCuestionario;

    @BeforeEach
    void setUp() {
        CuestionarioController controller = new CuestionarioController(gestorCuestionario);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/cuestionarios/{id}/instrucciones -> 200 OK")
    void obtenerInstrucciones_200() throws Exception {
        InstruccionesCuestionarioDTO dto = new InstruccionesCuestionarioDTO(
                6L,
                EstadoCuestionario.ACTIVO,
                "ARQUITECTO DE SOFTWARE",
                60,
                "Lea atentamente cada pregunta."
        );
        when(gestorCuestionario.obtenerInstrucciones(6L)).thenReturn(dto);

        mockMvc.perform(get("/api/cuestionarios/6/instrucciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.nombrePuesto").value("ARQUITECTO DE SOFTWARE"))
                .andExpect(jsonPath("$.tiempoMaximoMinutos").value(60))
                .andExpect(jsonPath("$.instrucciones").value("Lea atentamente cada pregunta."));
    }

    @Test
    @DisplayName("GET /api/cuestionarios/{id}/instrucciones con cuestionario ajeno o inexistente -> 404 Not Found")
    void obtenerInstrucciones_404() throws Exception {
        when(gestorCuestionario.obtenerInstrucciones(2L))
                .thenThrow(new RecursoNoEncontradoException("Cuestionario", 2L));

        mockMvc.perform(get("/api/cuestionarios/2/instrucciones"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    @DisplayName("GET /api/cuestionarios/{id}/instrucciones no disponible o vencido -> 422 Unprocessable Entity")
    void obtenerInstrucciones_422() throws Exception {
        when(gestorCuestionario.obtenerInstrucciones(8L))
                .thenThrow(new ReglaNegocioException("El cuestionario superó el tiempo máximo en estado activo."));

        mockMvc.perform(get("/api/cuestionarios/8/instrucciones"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"));
    }

    @Test
    @DisplayName("POST /api/cuestionarios/{id}/iniciar -> 200 OK con bloque actual y sin ponderaciones")
    void iniciarCuestionario_200() throws Exception {
        CuestionarioIniciadoDTO dto = new CuestionarioIniciadoDTO(
                6L,
                EstadoCuestionario.EN_PROCESO,
                LocalDateTime.of(2026, 11, 19, 15, 0, 0),
                3600L,
                2,
                new BloqueCuestionarioDTO(
                        1L,
                        1,
                        false,
                        List.of(
                                new PreguntaAsignadaDTO(
                                        1L,
                                        1,
                                        "¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?",
                                        List.of(
                                                new OpcionPreguntaCandidatoDTO(1L, 1, "UN PARADIGMA BASADO EN OBJETOS"),
                                                new OpcionPreguntaCandidatoDTO(2L, 2, "UN LENGUAJE DE PROGRAMACIÓN")
                                        ),
                                        null
                                )
                        )
                )
        );
        when(gestorCuestionario.iniciarCuestionario(6L)).thenReturn(dto);

        mockMvc.perform(post("/api/cuestionarios/6/iniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"))
                .andExpect(jsonPath("$.segundosRestantes").value(3600))
                .andExpect(jsonPath("$.totalBloques").value(2))
                .andExpect(jsonPath("$.bloqueActual.numero").value(1))
                .andExpect(jsonPath("$.bloqueActual.completado").value(false))
                .andExpect(jsonPath("$.bloqueActual.preguntas", hasSize(1)))
                .andExpect(jsonPath("$.bloqueActual.preguntas[0].preguntaAsignadaId").value(1))
                .andExpect(jsonPath("$.bloqueActual.preguntas[0].nroOrden").value(1))
                .andExpect(jsonPath("$.bloqueActual.preguntas[0].opciones", hasSize(2)))
                .andExpect(jsonPath("$.bloqueActual.preguntas[0].respuestaOpcionId").value(nullValue()))
                .andExpect(content().string(not(containsString("ponderacion"))));
    }

    @Test
    @DisplayName("POST /api/cuestionarios/{id}/iniciar con cuestionario ajeno o inexistente -> 404 Not Found")
    void iniciarCuestionario_404() throws Exception {
        when(gestorCuestionario.iniciarCuestionario(2L))
                .thenThrow(new RecursoNoEncontradoException("Cuestionario", 2L));

        mockMvc.perform(post("/api/cuestionarios/2/iniciar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    @DisplayName("POST /api/cuestionarios/{id}/iniciar con regla de negocio violada -> 422 Unprocessable Entity")
    void iniciarCuestionario_422() throws Exception {
        when(gestorCuestionario.iniciarCuestionario(10L))
                .thenThrow(new ReglaNegocioException("Se alcanzó la cantidad máxima de accesos permitidos (3)."));

        mockMvc.perform(post("/api/cuestionarios/10/iniciar"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"));
    }
}
