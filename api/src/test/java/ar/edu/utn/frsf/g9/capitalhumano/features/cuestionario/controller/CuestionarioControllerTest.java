package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.BloqueCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioIniciadoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.InstruccionesCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.OpcionPreguntaCandidatoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.PreguntaAsignadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.ResponderBloqueRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.ResponderBloqueResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ValidacionException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    @DisplayName("POST /api/cuestionarios/{id}/bloques/{numero}/respuestas -> 200 OK avance al siguiente bloque (8.A)")
    void responderBloque_200_avance() throws Exception {
        BloqueCuestionarioDTO siguiente = new BloqueCuestionarioDTO(
                2L,
                2,
                false,
                List.of(
                        new PreguntaAsignadaDTO(
                                4L,
                                4,
                                "¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?",
                                List.of(
                                        new OpcionPreguntaCandidatoDTO(3L, 1, "GARANTIZA UNA ÚNICA INSTANCIA"),
                                        new OpcionPreguntaCandidatoDTO(4L, 2, "CREA MUCHAS INSTANCIAS")
                                ),
                                null
                        )
                )
        );
        when(gestorCuestionario.responderBloque(eq(6L), eq(1), any(ResponderBloqueRequestDTO.class)))
                .thenReturn(ResponderBloqueResponseDTO.siguienteBloque(siguiente, 3500L));

        mockMvc.perform(post("/api/cuestionarios/6/bloques/1/respuestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "respuestas": [
                                    { "preguntaAsignadaId": 1, "opcionId": 1 },
                                    { "preguntaAsignadaId": 2, "opcionId": 30 },
                                    { "preguntaAsignadaId": 3, "opcionId": 26 }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completado").value(false))
                .andExpect(jsonPath("$.segundosRestantes").value(3500))
                .andExpect(jsonPath("$.bloqueSiguiente.numero").value(2))
                .andExpect(jsonPath("$.bloqueSiguiente.completado").value(false))
                .andExpect(jsonPath("$.bloqueSiguiente.preguntas", hasSize(1)))
                .andExpect(jsonPath("$.estado").doesNotExist())
                .andExpect(jsonPath("$.mensaje").doesNotExist())
                .andExpect(content().string(not(containsString("ponderacion"))));
    }

    @Test
    @DisplayName("POST /api/cuestionarios/{id}/bloques/{numero}/respuestas -> 200 OK cierre COMPLETO sin puntajes")
    void responderBloque_200_cierreCompleto() throws Exception {
        when(gestorCuestionario.responderBloque(eq(12L), eq(1), any(ResponderBloqueRequestDTO.class)))
                .thenReturn(ResponderBloqueResponseDTO.cuestionarioCompletado());

        mockMvc.perform(post("/api/cuestionarios/12/bloques/1/respuestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "respuestas": [
                                    { "preguntaAsignadaId": 13, "opcionId": 1 }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completado").value(true))
                .andExpect(jsonPath("$.estado").value("COMPLETO"))
                .andExpect(jsonPath("$.mensaje").value("El cuestionario ha sido completado con éxito"))
                .andExpect(jsonPath("$.bloqueSiguiente").doesNotExist())
                .andExpect(jsonPath("$.segundosRestantes").doesNotExist())
                .andExpect(content().string(not(containsString("puntaje"))));
    }

    @Test
    @DisplayName("POST /api/cuestionarios/{id}/bloques/{numero}/respuestas -> 400 VALIDACION con todos los detalles (Flujo 6.A)")
    void responderBloque_400_flujo6A_conDetalles() throws Exception {
        when(gestorCuestionario.responderBloque(eq(6L), eq(1), any(ResponderBloqueRequestDTO.class)))
                .thenThrow(new ValidacionException(
                        "Debe responder todas las preguntas del bloque antes de continuar.",
                        List.of(
                                new ValidacionException.DetalleError("2", "Debe responder la pregunta N.° 2 (preguntaAsignadaId: 2)."),
                                new ValidacionException.DetalleError("3", "Debe responder la pregunta N.° 3 (preguntaAsignadaId: 3).")
                        )
                ));

        mockMvc.perform(post("/api/cuestionarios/6/bloques/1/respuestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "respuestas": [
                                    { "preguntaAsignadaId": 1, "opcionId": 1 }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles", hasSize(2)))
                .andExpect(jsonPath("$.detalles[0].campo").value("2"))
                .andExpect(jsonPath("$.detalles[1].campo").value("3"));
    }

    @Test
    @DisplayName("POST /api/cuestionarios/{id}/bloques/{numero}/respuestas -> 404 Not Found")
    void responderBloque_404() throws Exception {
        when(gestorCuestionario.responderBloque(eq(2L), eq(1), any(ResponderBloqueRequestDTO.class)))
                .thenThrow(new RecursoNoEncontradoException("Cuestionario", 2L));

        mockMvc.perform(post("/api/cuestionarios/2/bloques/1/respuestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"respuestas\": []}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    @DisplayName("POST /api/cuestionarios/{id}/bloques/{numero}/respuestas -> 422 Unprocessable Entity")
    void responderBloque_422() throws Exception {
        when(gestorCuestionario.responderBloque(eq(7L), eq(1), any(ResponderBloqueRequestDTO.class)))
                .thenThrow(new ReglaNegocioException("El bloque 1 ya fue respondido anteriormente."));

        mockMvc.perform(post("/api/cuestionarios/7/bloques/1/respuestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"respuestas\": []}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"));
    }
}
