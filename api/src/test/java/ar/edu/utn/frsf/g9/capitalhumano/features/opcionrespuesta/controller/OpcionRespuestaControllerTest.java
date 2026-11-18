package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.ItemOpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.OpcionRespuestaResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.gestor.GestorOpcionRespuesta;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests para {@link OpcionRespuestaController} utilizando MockMvc (CU 024 / CU 029).
 */
@ExtendWith(MockitoExtension.class)
class OpcionRespuestaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestorOpcionRespuesta gestorOpcionRespuesta;

    @BeforeEach
    void setUp() {
        OpcionRespuestaController controller = new OpcionRespuestaController(gestorOpcionRespuesta);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/opciones-respuesta con datos válidos retorna HTTP 201 Created y el DTO creado con id")
    void crearOpcionRespuesta_exitoso201() throws Exception {
        OpcionRespuestaDTO respuesta = new OpcionRespuestaDTO(
                3L,
                "CALIFICACIÓN",
                "ESCALA DE CALIFICACIÓN DEL DESEMPEÑO",
                List.of(
                        new ItemOpcionRespuestaDTO(1, "MALO"),
                        new ItemOpcionRespuestaDTO(2, "REGULAR"),
                        new ItemOpcionRespuestaDTO(3, "BUENO")
                )
        );

        when(gestorOpcionRespuesta.crearOpcionRespuesta(any(OpcionRespuestaDTO.class))).thenReturn(respuesta);

        String jsonRequest = """
                {
                  "nombre": "Calificación",
                  "descripcion": "Escala de calificación del desempeño",
                  "items": [
                    { "orden": 1, "descripcion": "Malo" },
                    { "orden": 2, "descripcion": "Regular" },
                    { "orden": 3, "descripcion": "Bueno" }
                  ]
                }
                """;

        mockMvc.perform(post("/api/opciones-respuesta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nombre").value("CALIFICACIÓN"))
                .andExpect(jsonPath("$.descripcion").value("ESCALA DE CALIFICACIÓN DEL DESEMPEÑO"))
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[0].orden").value(1))
                .andExpect(jsonPath("$.items[0].descripcion").value("MALO"))
                .andExpect(jsonPath("$.items[1].orden").value(2))
                .andExpect(jsonPath("$.items[1].descripcion").value("REGULAR"))
                .andExpect(jsonPath("$.items[2].orden").value(3))
                .andExpect(jsonPath("$.items[2].descripcion").value("BUENO"));

        verify(gestorOpcionRespuesta).crearOpcionRespuesta(any(OpcionRespuestaDTO.class));
    }

    @Test
    @DisplayName("POST /api/opciones-respuesta con campos vacíos e ítems incompletos retorna HTTP 400 con todos los detalles juntos")
    void crearOpcionRespuesta_camposVaciosEItemsIncompletos400() throws Exception {
        String jsonRequest = """
                {
                  "nombre": "",
                  "descripcion": "   ",
                  "items": [
                    { "orden": null, "descripcion": "" },
                    { "orden": 2, "descripcion": "   " }
                  ]
                }
                """;

        mockMvc.perform(post("/api/opciones-respuesta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.path").value("/api/opciones-respuesta"))
                .andExpect(jsonPath("$.detalles", hasSize(5)))
                .andExpect(jsonPath("$.detalles[*].campo",
                        containsInAnyOrder(
                                "nombre",
                                "descripcion",
                                "items[0].orden",
                                "items[0].descripcion",
                                "items[1].descripcion"
                        )));

        verify(gestorOpcionRespuesta, never()).crearOpcionRespuesta(any());
    }

    @Test
    @DisplayName("POST /api/opciones-respuesta con menos de 2 ítems retorna HTTP 400 Bad Request")
    void crearOpcionRespuesta_menosDeDosItems400() throws Exception {
        String jsonRequest = """
                {
                  "nombre": "Escala Corta",
                  "descripcion": "Descripción válida",
                  "items": [
                    { "orden": 1, "descripcion": "Único ítem" }
                  ]
                }
                """;

        mockMvc.perform(post("/api/opciones-respuesta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles", hasSize(1)))
                .andExpect(jsonPath("$.detalles[0].campo").value("items"));

        verify(gestorOpcionRespuesta, never()).crearOpcionRespuesta(any());
    }

    @Test
    @DisplayName("POST /api/opciones-respuesta con nombre ya existente retorna HTTP 409 Conflict")
    void crearOpcionRespuesta_nombreDuplicado409() throws Exception {
        when(gestorOpcionRespuesta.crearOpcionRespuesta(any(OpcionRespuestaDTO.class)))
                .thenThrow(new RecursoDuplicadoException("Ya existe una opción de respuesta con el nombre 'FRECUENCIA'"));

        String jsonRequest = """
                {
                  "nombre": "Frecuencia",
                  "descripcion": "Escala de frecuencia",
                  "items": [
                    { "orden": 1, "descripcion": "Nunca" },
                    { "orden": 2, "descripcion": "Siempre" }
                  ]
                }
                """;

        mockMvc.perform(post("/api/opciones-respuesta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.codigo").value("RECURSO_DUPLICADO"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe una opción de respuesta con el nombre 'FRECUENCIA'"))
                .andExpect(jsonPath("$.path").value("/api/opciones-respuesta"));
    }

    @Test
    @DisplayName("POST /api/opciones-respuesta con regla de negocio violada retorna HTTP 422 Unprocessable Entity")
    void crearOpcionRespuesta_reglaNegocioViolada422() throws Exception {
        when(gestorOpcionRespuesta.crearOpcionRespuesta(any(OpcionRespuestaDTO.class)))
                .thenThrow(new ReglaNegocioException("El orden de visualización de los ítems debe ser consecutivo desde 1 hasta 2 sin huecos (falta el orden 2)"));

        String jsonRequest = """
                {
                  "nombre": "Escala Con Hueco",
                  "descripcion": "Descripción válida",
                  "items": [
                    { "orden": 1, "descripcion": "Bajo" },
                    { "orden": 3, "descripcion": "Alto" }
                  ]
                }
                """;

        mockMvc.perform(post("/api/opciones-respuesta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"))
                .andExpect(jsonPath("$.mensaje").value("El orden de visualización de los ítems debe ser consecutivo desde 1 hasta 2 sin huecos (falta el orden 2)"))
                .andExpect(jsonPath("$.path").value("/api/opciones-respuesta"));
    }

    @Test
    @DisplayName("GET /api/opciones-respuesta?nombre=frecuencia retorna HTTP 200 OK con lista de resúmenes filtrada")
    void buscarOpcionesRespuesta_conFiltro200() throws Exception {
        List<OpcionRespuestaResumenDTO> respuesta = List.of(
                new OpcionRespuestaResumenDTO(1L, "FRECUENCIA", "ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO"),
                new OpcionRespuestaResumenDTO(3L, "FRECUENCIA SEMANAL", "ESCALA DE FRECUENCIA DE EJECUCIÓN SEMANAL DE TAREAS")
        );

        when(gestorOpcionRespuesta.buscarOpcionesRespuesta("frecuencia")).thenReturn(respuesta);

        mockMvc.perform(get("/api/opciones-respuesta")
                        .param("nombre", "frecuencia")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("FRECUENCIA"))
                .andExpect(jsonPath("$[0].descripcion").value("ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO"))
                .andExpect(jsonPath("$[0].items").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(3))
                .andExpect(jsonPath("$[1].nombre").value("FRECUENCIA SEMANAL"))
                .andExpect(jsonPath("$[1].descripcion").value("ESCALA DE FRECUENCIA DE EJECUCIÓN SEMANAL DE TAREAS"))
                .andExpect(jsonPath("$[1].items").doesNotExist());

        verify(gestorOpcionRespuesta).buscarOpcionesRespuesta("frecuencia");
    }

    @Test
    @DisplayName("GET /api/opciones-respuesta sin filtro retorna HTTP 200 OK con todas las opciones de respuesta")
    void buscarOpcionesRespuesta_sinFiltro200() throws Exception {
        List<OpcionRespuestaResumenDTO> respuesta = List.of(
                new OpcionRespuestaResumenDTO(2L, "ACUERDO", "ESCALA DE GRADO DE ACUERDO CON LA AFIRMACIÓN"),
                new OpcionRespuestaResumenDTO(1L, "FRECUENCIA", "ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO"),
                new OpcionRespuestaResumenDTO(3L, "FRECUENCIA SEMANAL", "ESCALA DE FRECUENCIA DE EJECUCIÓN SEMANAL DE TAREAS"),
                new OpcionRespuestaResumenDTO(4L, "SATISFACCIÓN", "ESCALA DE NIVEL DE SATISFACCIÓN GENERAL")
        );

        when(gestorOpcionRespuesta.buscarOpcionesRespuesta(null)).thenReturn(respuesta);

        mockMvc.perform(get("/api/opciones-respuesta")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].nombre").value("ACUERDO"))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].nombre").value("FRECUENCIA"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].nombre").value("FRECUENCIA SEMANAL"))
                .andExpect(jsonPath("$[3].id").value(4))
                .andExpect(jsonPath("$[3].nombre").value("SATISFACCIÓN"));

        verify(gestorOpcionRespuesta).buscarOpcionesRespuesta(null);
    }

    @Test
    @DisplayName("GET /api/opciones-respuesta sin coincidencias retorna HTTP 200 OK con lista vacía")
    void buscarOpcionesRespuesta_listaVacia200() throws Exception {
        when(gestorOpcionRespuesta.buscarOpcionesRespuesta("inexistente")).thenReturn(List.of());

        mockMvc.perform(get("/api/opciones-respuesta")
                        .param("nombre", "inexistente")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(gestorOpcionRespuesta).buscarOpcionesRespuesta("inexistente");
    }

    @Test
    @DisplayName("GET /api/opciones-respuesta/{id} existente retorna HTTP 200 OK con detalle e ítems ordenados")
    void obtenerOpcionRespuestaPorId_existente200() throws Exception {
        OpcionRespuestaDTO detalle = new OpcionRespuestaDTO(
                1L,
                "FRECUENCIA",
                "ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO",
                List.of(
                        new ItemOpcionRespuestaDTO(1, "NUNCA"),
                        new ItemOpcionRespuestaDTO(2, "A VECES"),
                        new ItemOpcionRespuestaDTO(3, "SIEMPRE")
                )
        );

        when(gestorOpcionRespuesta.obtenerPorId(1L)).thenReturn(detalle);

        mockMvc.perform(get("/api/opciones-respuesta/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("FRECUENCIA"))
                .andExpect(jsonPath("$.descripcion").value("ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO"))
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[0].orden").value(1))
                .andExpect(jsonPath("$.items[0].descripcion").value("NUNCA"))
                .andExpect(jsonPath("$.items[1].orden").value(2))
                .andExpect(jsonPath("$.items[1].descripcion").value("A VECES"))
                .andExpect(jsonPath("$.items[2].orden").value(3))
                .andExpect(jsonPath("$.items[2].descripcion").value("SIEMPRE"));

        verify(gestorOpcionRespuesta).obtenerPorId(1L);
    }

    @Test
    @DisplayName("GET /api/opciones-respuesta/{id} inexistente retorna HTTP 404 Not Found con ApiError RECURSO_NO_ENCONTRADO")
    void obtenerOpcionRespuestaPorId_inexistente404() throws Exception {
        when(gestorOpcionRespuesta.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Opción de respuesta", 99L));

        mockMvc.perform(get("/api/opciones-respuesta/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensaje").value("Opción de respuesta con id 99 no existe"))
                .andExpect(jsonPath("$.path").value("/api/opciones-respuesta/99"));

        verify(gestorOpcionRespuesta).obtenerPorId(99L);
    }
}

