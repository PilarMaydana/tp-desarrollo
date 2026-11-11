package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor.GestorPregunta;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests para {@link PreguntaController} con MockMvc (CU 020 / CU 021).
 */
@ExtendWith(MockitoExtension.class)
class PreguntaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestorPregunta gestorPregunta;

    @BeforeEach
    void setUp() {
        PreguntaController controller = new PreguntaController(gestorPregunta);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/preguntas con criterio 'nombre' retorna 200 OK y la lista de preguntas")
    void buscarPreguntas_conParametro_retorna200() throws Exception {
        PreguntaDTO item = new PreguntaDTO(
                1L,
                "Competencias Técnicas",
                "Desarrollo de Software",
                "Pregunta POO",
                "¿Qué es la POO?",
                "Conceptos básicos",
                List.of(
                        new OpcionRespuestaDTO("Opcion A", "Paradigma", 1.0),
                        new OpcionRespuestaDTO("Opcion B", "Lenguaje", 0.0)
                )
        );

        when(gestorPregunta.buscarPreguntas(any())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/preguntas")
                        .param("nombre", "Programacion")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].competencia").value("Competencias Técnicas"))
                .andExpect(jsonPath("$[0].factor").value("Desarrollo de Software"))
                .andExpect(jsonPath("$[0].nombre").value("Pregunta POO"))
                .andExpect(jsonPath("$[0].enunciado").value("¿Qué es la POO?"))
                .andExpect(jsonPath("$[0].opciones", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/preguntas sin criterios de búsqueda retorna 400 Bad Request")
    void buscarPreguntas_sinCriterios_retorna400() throws Exception {
        when(gestorPregunta.buscarPreguntas(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe ingresar al menos un criterio de búsqueda"));

        mockMvc.perform(get("/api/preguntas")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/preguntas/{id} existente retorna 200 OK con los datos")
    void obtenerPregunta_existente_retorna200() throws Exception {
        PreguntaDTO dto = new PreguntaDTO(
                5L, "COMP", "FACTOR", "Nombre", "Enunciado", "Desc",
                List.of(new OpcionRespuestaDTO("Op A", "Desc", 1.0), new OpcionRespuestaDTO("Op B", "Desc", 0.0))
        );

        when(gestorPregunta.obtenerPorId(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/preguntas/5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nombre").value("Nombre"));
    }

    @Test
    @DisplayName("GET /api/preguntas/{id} inexistente retorna 404 Not Found")
    void obtenerPregunta_inexistente_retorna404() throws Exception {
        when(gestorPregunta.obtenerPorId(99L)).thenThrow(new RecursoNoEncontradoException("Pregunta", 99L));

        mockMvc.perform(get("/api/preguntas/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/preguntas con datos válidos retorna 201 Created y el DTO creado")
    void crearPregunta_valida_retorna201() throws Exception {
        PreguntaDTO respuesta = new PreguntaDTO(
                1L,
                "Competencias Técnicas",
                "Desarrollo de Software",
                "FAC-01",
                "Patrones de Diseño",
                "¿Para qué sirve el patrón Singleton?",
                "Pregunta de arquitectura",
                List.of(
                        new OpcionRespuestaDTO("Instancia única", "Garantiza una única instancia", 5.0),
                        new OpcionRespuestaDTO("Múltiples instancias", "Crea muchas instancias", 5.0)
                )
        );

        when(gestorPregunta.crearPregunta(any())).thenReturn(respuesta);

        String jsonValido = """
                {
                  "factorCodigo": "FAC-01",
                  "nombre": "Patrones de Diseño",
                  "enunciado": "¿Para qué sirve el patrón Singleton?",
                  "descripcion": "Pregunta de arquitectura",
                  "opciones": [
                    { "nombre": "Instancia única", "descripcion": "Garantiza una única instancia", "ponderacion": 5.0 },
                    { "nombre": "Múltiples instancias", "descripcion": "Crea muchas instancias", "ponderacion": 5.0 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/preguntas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Patrones de Diseño"))
                .andExpect(jsonPath("$.opciones", hasSize(2)));
    }

    @Test
    @DisplayName("POST /api/preguntas con formato CU-021 (codigoFactor y opcionesRespuesta) retorna HTTP 201 Created")
    void crearPregunta_exitoso201_cu021() throws Exception {
        List<OpcionRespuestaDTO> opciones = List.of(
                new OpcionRespuestaDTO(1L, 1, "NUNCA", "NUNCA TRANSMITE", 0.0),
                new OpcionRespuestaDTO(2L, 2, "SIEMPRE", "SIEMPRE TRANSMITE", 10.0)
        );
        PreguntaDTO respuesta = new PreguntaDTO(1L, "COMUNICACIÓN ASERTIVA", "¿CÓMO COMUNICA SUS IDEAS?", "DESCRIPCION", "FAC-COM-01", opciones);
        respuesta.setCodigoCompetencia("COM-01");

        when(gestorPregunta.crearPregunta(any(PreguntaDTO.class))).thenReturn(respuesta);

        String jsonRequest = """
                {
                  "nombre": "Comunicación Asertiva",
                  "enunciado": "¿Cómo comunica sus ideas?",
                  "descripcion": "Descripción",
                  "codigoFactor": "FAC-COM-01",
                  "opcionesRespuesta": [
                    {
                      "orden": 1,
                      "nombre": "Nunca",
                      "descripcion": "Nunca transmite",
                      "ponderacion": 0.0
                    },
                    {
                      "orden": 2,
                      "nombre": "Siempre",
                      "descripcion": "Siempre transmite",
                      "ponderacion": 10.0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/preguntas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("COMUNICACIÓN ASERTIVA"))
                .andExpect(jsonPath("$.enunciado").value("¿CÓMO COMUNICA SUS IDEAS?"))
                .andExpect(jsonPath("$.codigoFactor").value("FAC-COM-01"))
                .andExpect(jsonPath("$.opcionesRespuesta", hasSize(2)));

        verify(gestorPregunta).crearPregunta(any(PreguntaDTO.class));
    }

    @Test
    @DisplayName("POST /api/preguntas con campos vacíos o menos de 2 opciones retorna 400 Bad Request")
    void crearPregunta_invalida_retorna400() throws Exception {
        String jsonInvalido = """
                {
                  "factorCodigo": "",
                  "nombre": "",
                  "enunciado": "",
                  "opciones": []
                }
                """;

        mockMvc.perform(post("/api/preguntas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalles").isArray());

        verify(gestorPregunta, never()).crearPregunta(any());
    }

    @Test
    @DisplayName("POST /api/preguntas con nombre duplicado retorna 409 Conflict")
    void crearPregunta_duplicada_retorna409() throws Exception {
        when(gestorPregunta.crearPregunta(any()))
                .thenThrow(new RecursoDuplicadoException("Ya existe una pregunta con el nombre 'POO'"));

        String json = """
                {
                  "factorCodigo": "FAC-01",
                  "nombre": "POO",
                  "enunciado": "¿Qué es la POO?",
                  "opciones": [
                    { "nombre": "A", "ponderacion": 5.0 },
                    { "nombre": "B", "ponderacion": 5.0 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/preguntas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/preguntas con ponderaciones inválidas retorna HTTP 422 Unprocessable Entity")
    void crearPregunta_reglaNegocioViolada422() throws Exception {
        when(gestorPregunta.crearPregunta(any(PreguntaDTO.class)))
                .thenThrow(new ReglaNegocioException("La suma de las ponderaciones de las opciones de respuesta debe ser exactamente 10 puntos (suma actual: 8.0)"));

        String jsonRequest = """
                {
                  "nombre": "Pregunta Invalida",
                  "enunciado": "¿Pregunta invalida?",
                  "codigoFactor": "FAC-COM-01",
                  "opcionesRespuesta": [
                    {
                      "orden": 1,
                      "nombre": "Opción 1",
                      "descripcion": "Desc 1",
                      "ponderacion": 4.0
                    },
                    {
                      "orden": 2,
                      "nombre": "Opción 2",
                      "descripcion": "Desc 2",
                      "ponderacion": 4.0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/preguntas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"))
                .andExpect(jsonPath("$.mensaje").value("La suma de las ponderaciones de las opciones de respuesta debe ser exactamente 10 puntos (suma actual: 8.0)"));
    }

    @Test
    @DisplayName("PUT /api/preguntas/{id} con datos válidos retorna 200 OK y la pregunta modificada")
    void modificarPregunta_valida_retorna200() throws Exception {
        PreguntaDTO modificada = new PreguntaDTO(
                1L, "Competencias Técnicas", "Desarrollo", "FAC-01",
                "Nombre Modificado", "Enunciado Modificado", "Desc",
                List.of(
                        new OpcionRespuestaDTO("Op 1", "Desc", 1.0),
                        new OpcionRespuestaDTO("Op 2", "Desc", 0.0)
                )
        );

        when(gestorPregunta.modificarPregunta(eq(1L), any())).thenReturn(modificada);

        String json = """
                {
                  "nombre": "Nombre Modificado",
                  "enunciado": "Enunciado Modificado",
                  "descripcion": "Desc",
                  "opciones": [
                    { "nombre": "Op 1", "descripcion": "Desc", "ponderacion": 1.0 },
                    { "nombre": "Op 2", "descripcion": "Desc", "ponderacion": 0.0 }
                  ]
                }
                """;

        mockMvc.perform(put("/api/preguntas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Nombre Modificado"));
    }

    @Test
    @DisplayName("PUT /api/preguntas/{id} inexistente retorna 404 Not Found")
    void modificarPregunta_inexistente_retorna404() throws Exception {
        when(gestorPregunta.modificarPregunta(eq(99L), any()))
                .thenThrow(new RecursoNoEncontradoException("Pregunta", 99L));

        String json = """
                {
                  "nombre": "Nombre",
                  "enunciado": "Enunciado",
                  "opciones": [
                    { "nombre": "Op 1", "ponderacion": 1.0 },
                    { "nombre": "Op 2", "ponderacion": 0.0 }
                  ]
                }
                """;

        mockMvc.perform(put("/api/preguntas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/preguntas/{id} existente retorna 204 No Content")
    void eliminarPregunta_existente_retorna204() throws Exception {
        doNothing().when(gestorPregunta).eliminarPregunta(1L);

        mockMvc.perform(delete("/api/preguntas/1"))
                .andExpect(status().isNoContent());

        verify(gestorPregunta).eliminarPregunta(1L);
    }

    @Test
    @DisplayName("DELETE /api/preguntas/{id} inexistente retorna 404 Not Found")
    void eliminarPregunta_inexistente_retorna404() throws Exception {
        doThrow(new RecursoNoEncontradoException("Pregunta", 99L))
                .when(gestorPregunta).eliminarPregunta(99L);

        mockMvc.perform(delete("/api/preguntas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
