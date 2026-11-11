package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor.GestorPregunta;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
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

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para {@link PreguntaController} utilizando MockMvc.
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
    @DisplayName("POST /api/preguntas con datos válidos retorna HTTP 201 Created y el DTO creado")
    void crearPregunta_exitoso201() throws Exception {
        List<OpcionRespuestaDTO> opciones = List.of(
                new OpcionRespuestaDTO(1L, 1, "NUNCA", "NUNCA TRANSMITE", 0.0),
                new OpcionRespuestaDTO(2L, 2, "SIEMPRE", "SIEMPRE TRANSMITE", 10.0)
        );
        PreguntaDTO respuesta = new PreguntaDTO(1L, "COMUNICACIÓN ASERTIVA", "¿CÓMO COMUNICA SUS IDEAS?", "DESCRIPCION", "FAC-COM-01", "COM-01", opciones);

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
                .andExpect(jsonPath("$.opcionesRespuesta", hasSize(2)))
                .andExpect(jsonPath("$.opcionesRespuesta[0].ponderacion").value(0.0))
                .andExpect(jsonPath("$.opcionesRespuesta[1].ponderacion").value(10.0));

        verify(gestorPregunta).crearPregunta(any(PreguntaDTO.class));
    }

    @Test
    @DisplayName("POST /api/preguntas con campos vacíos retorna HTTP 400 Bad Request con todos los detalles de validación")
    void crearPregunta_camposVacios400ConDetallesCompletos() throws Exception {
        String jsonRequest = """
                {
                  "nombre": "",
                  "enunciado": "   ",
                  "codigoFactor": "",
                  "opcionesRespuesta": []
                }
                """;

        mockMvc.perform(post("/api/preguntas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.path").value("/api/preguntas"))
                .andExpect(jsonPath("$.detalles", hasSize(4)))
                .andExpect(jsonPath("$.detalles[*].campo",
                        containsInAnyOrder("nombre", "enunciado", "codigoFactor", "opcionesRespuesta")));

        verify(gestorPregunta, never()).crearPregunta(any());
    }

    @Test
    @DisplayName("POST /api/preguntas con nombre o enunciado duplicado retorna HTTP 409 Conflict")
    void crearPregunta_recursoDuplicado409() throws Exception {
        when(gestorPregunta.crearPregunta(any(PreguntaDTO.class)))
                .thenThrow(new RecursoDuplicadoException("Ya existe una pregunta con el nombre 'COMUNICACIÓN ASERTIVA' para el factor seleccionado"));

        String jsonRequest = """
                {
                  "nombre": "Comunicación Asertiva",
                  "enunciado": "¿Cómo transmite?",
                  "codigoFactor": "FAC-COM-01",
                  "opcionesRespuesta": [
                    {
                      "orden": 1,
                      "nombre": "Nunca",
                      "descripcion": "Desc 1",
                      "ponderacion": 5.0
                    },
                    {
                      "orden": 2,
                      "nombre": "Siempre",
                      "descripcion": "Desc 2",
                      "ponderacion": 5.0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/preguntas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.codigo").value("RECURSO_DUPLICADO"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe una pregunta con el nombre 'COMUNICACIÓN ASERTIVA' para el factor seleccionado"));
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
}

