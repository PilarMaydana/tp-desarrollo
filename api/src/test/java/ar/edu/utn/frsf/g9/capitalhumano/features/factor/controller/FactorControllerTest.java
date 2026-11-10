package ar.edu.utn.frsf.g9.capitalhumano.features.factor.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dto.FactorDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.gestor.GestorFactor;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
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
 * Tests de integración para {@link FactorController} utilizando MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class FactorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestorFactor gestorFactor;

    @BeforeEach
    void setUp() {
        FactorController controller = new FactorController(gestorFactor);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/factores con datos válidos retorna 201 Created y el FactorDTO creado")
    void crearFactor_exitoso201() throws Exception {
        FactorDTO respuesta = new FactorDTO(
                1L,
                "FAC-01",
                "COMUNICACIÓN ORAL",
                "HABILIDAD PARA TRANSMITIR IDEAS VERBALMENTE",
                1L,
                "COMUNICACIÓN",
                1
        );

        when(gestorFactor.crearFactor(any(FactorDTO.class))).thenReturn(respuesta);

        String jsonRequest = """
                {
                  "codigo": "FAC-01",
                  "nombre": "Comunicación Oral",
                  "descripcion": "Habilidad para transmitir ideas verbalmente",
                  "competenciaId": 1,
                  "nroOrden": 1
                }
                """;

        mockMvc.perform(post("/api/factores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("FAC-01"))
                .andExpect(jsonPath("$.nombre").value("COMUNICACIÓN ORAL"))
                .andExpect(jsonPath("$.descripcion").value("HABILIDAD PARA TRANSMITIR IDEAS VERBALMENTE"))
                .andExpect(jsonPath("$.competenciaId").value(1))
                .andExpect(jsonPath("$.nroOrden").value(1));

        verify(gestorFactor).crearFactor(any(FactorDTO.class));
    }

    @Test
    @DisplayName("Flujo 2.A: POST /api/factores con cuerpo vacío retorna 400 Bad Request y todos los detalles en ApiError")
    void crearFactor_cuerpoVacioRetorna400ConTodosLosDetalles() throws Exception {
        String jsonRequest = "{}";

        mockMvc.perform(post("/api/factores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.path").value("/api/factores"))
                .andExpect(jsonPath("$.detalles", hasSize(5)))
                .andExpect(jsonPath("$.detalles[*].campo",
                        containsInAnyOrder("codigo", "nombre", "descripcion", "competenciaId", "nroOrden")));

        verify(gestorFactor, never()).crearFactor(any());
    }

    @Test
    @DisplayName("Flujo 2.A: POST /api/factores con nroOrden no positivo retorna 400 Bad Request")
    void crearFactor_nroOrdenInvalidoRetorna400() throws Exception {
        String jsonRequest = """
                {
                  "codigo": "FAC-01",
                  "nombre": "Comunicación Oral",
                  "descripcion": "Habilidad para transmitir ideas verbalmente",
                  "competenciaId": 1,
                  "nroOrden": 0
                }
                """;

        mockMvc.perform(post("/api/factores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles[0].campo").value("nroOrden"));

        verify(gestorFactor, never()).crearFactor(any());
    }

    @Test
    @DisplayName("Flujo 2.B: Conflicto de unicidad en código retorna 409 Conflict")
    void crearFactor_codigoDuplicadoRetorna409() throws Exception {
        when(gestorFactor.crearFactor(any(FactorDTO.class)))
                .thenThrow(new RecursoDuplicadoException("Ya existe un factor con el código 'FAC-01'"));

        String jsonRequest = """
                {
                  "codigo": "FAC-01",
                  "nombre": "Otro Factor",
                  "descripcion": "Descripción válida",
                  "competenciaId": 1,
                  "nroOrden": 3
                }
                """;

        mockMvc.perform(post("/api/factores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.codigo").value("RECURSO_DUPLICADO"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe un factor con el código 'FAC-01'"));
    }

    @Test
    @DisplayName("Flujo 2.C: Conflicto de unicidad en nroOrden por competencia retorna 409 Conflict")
    void crearFactor_nroOrdenDuplicadoRetorna409() throws Exception {
        when(gestorFactor.crearFactor(any(FactorDTO.class)))
                .thenThrow(new RecursoDuplicadoException(
                        "Ya existe un factor con el número de orden 1 para la competencia seleccionada"));

        String jsonRequest = """
                {
                  "codigo": "FAC-99",
                  "nombre": "Nuevo Factor",
                  "descripcion": "Descripción válida",
                  "competenciaId": 1,
                  "nroOrden": 1
                }
                """;

        mockMvc.perform(post("/api/factores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.codigo").value("RECURSO_DUPLICADO"))
                .andExpect(jsonPath("$.mensaje").value(
                        "Ya existe un factor con el número de orden 1 para la competencia seleccionada"));
    }

    @Test
    @DisplayName("Relaciones: Competencia inexistente retorna 404 Not Found")
    void crearFactor_competenciaInexistenteRetorna404() throws Exception {
        when(gestorFactor.crearFactor(any(FactorDTO.class)))
                .thenThrow(new RecursoNoEncontradoException("Competencia", 999L));

        String jsonRequest = """
                {
                  "codigo": "FAC-99",
                  "nombre": "Nuevo Factor",
                  "descripcion": "Descripción válida",
                  "competenciaId": 999,
                  "nroOrden": 1
                }
                """;

        mockMvc.perform(post("/api/factores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensaje").value("Competencia con id 999 no existe"));
    }

    @Test
    @DisplayName("GET /api/factores retorna 200 OK y la lista de factores")
    void listarFactores_exitoso200() throws Exception {
        FactorDTO factor = new FactorDTO(1L, "FAC-01", "COMUNICACIÓN ORAL", "DESC", 1L, "COMUNICACIÓN", 1);
        when(gestorFactor.listarFactores()).thenReturn(List.of(factor));

        mockMvc.perform(get("/api/factores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].codigo").value("FAC-01"));
    }
}
