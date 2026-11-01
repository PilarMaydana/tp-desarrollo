package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto.CaracteristicaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto.PuestoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.gestor.GestorPuesto;
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
 * Tests de integración para {@link PuestoController} utilizando MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class PuestoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestorPuesto gestorPuesto;

    @BeforeEach
    void setUp() {
        PuestoController controller = new PuestoController(gestorPuesto);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/puestos con datos válidos retorna HTTP 201 Created y el DTO creado")
    void crearPuesto_exitoso201() throws Exception {
        CaracteristicaDTO car1 = new CaracteristicaDTO(1L, 1L, "COMUNICACIÓN", 8.0);
        CaracteristicaDTO car2 = new CaracteristicaDTO(2L, 2L, "LIDERAZGO", 9.0);
        PuestoDTO respuesta = new PuestoDTO(1L, "PUE-01", "DESARROLLADOR SENIOR", "DESCRIPCIÓN DEL PUESTO", "ACME CORP", List.of(car1, car2));

        when(gestorPuesto.crearPuesto(any(PuestoDTO.class))).thenReturn(respuesta);

        String jsonRequest = """
                {
                  "codigo": "PUE-01",
                  "nombre": "Desarrollador Senior",
                  "descripcion": "Descripción del puesto",
                  "empresa": "ACME Corp",
                  "caracteristicas": [
                    {
                      "competenciaId": 1,
                      "ponderacion": 8.0
                    },
                    {
                      "competenciaId": 2,
                      "ponderacion": 9.0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/puestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("PUE-01"))
                .andExpect(jsonPath("$.nombre").value("DESARROLLADOR SENIOR"))
                .andExpect(jsonPath("$.descripcion").value("DESCRIPCIÓN DEL PUESTO"))
                .andExpect(jsonPath("$.empresa").value("ACME CORP"))
                .andExpect(jsonPath("$.caracteristicas", hasSize(2)))
                .andExpect(jsonPath("$.caracteristicas[0].nombreCompetencia").value("COMUNICACIÓN"))
                .andExpect(jsonPath("$.caracteristicas[1].nombreCompetencia").value("LIDERAZGO"));

        verify(gestorPuesto).crearPuesto(any(PuestoDTO.class));
    }

    @Test
    @DisplayName("POST /api/puestos con múltiples campos vacíos retorna HTTP 400 Bad Request detallando todas las omisiones")
    void crearPuesto_camposVacios400ConDetallesCompletos() throws Exception {
        String jsonRequest = """
                {
                  "codigo": "",
                  "nombre": "   ",
                  "descripcion": null,
                  "empresa": "",
                  "caracteristicas": []
                }
                """;

        mockMvc.perform(post("/api/puestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.path").value("/api/puestos"))
                .andExpect(jsonPath("$.detalles", hasSize(5)))
                .andExpect(jsonPath("$.detalles[*].campo",
                        containsInAnyOrder("codigo", "nombre", "descripcion", "empresa", "caracteristicas")));

        verify(gestorPuesto, never()).crearPuesto(any());
    }

    @Test
    @DisplayName("POST /api/puestos con ponderación fuera de rango retorna HTTP 400 Bad Request")
    void crearPuesto_ponderacionFueraDeRango400() throws Exception {
        String jsonRequest = """
                {
                  "codigo": "PUE-01",
                  "nombre": "Analista QA",
                  "descripcion": "Descripción",
                  "empresa": "Tech",
                  "caracteristicas": [
                    {
                      "competenciaId": 1,
                      "ponderacion": 15.0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/puestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles", hasSize(1)))
                .andExpect(jsonPath("$.detalles[0].campo").value("caracteristicas[0].ponderacion"));

        verify(gestorPuesto, never()).crearPuesto(any());
    }

    @Test
    @DisplayName("POST /api/puestos con código o nombre duplicado retorna HTTP 409 Conflict")
    void crearPuesto_recursoDuplicado409() throws Exception {
        when(gestorPuesto.crearPuesto(any(PuestoDTO.class)))
                .thenThrow(new RecursoDuplicadoException("Ya existe un puesto con el código 'DEV-01'"));

        String jsonRequest = """
                {
                  "codigo": "DEV-01",
                  "nombre": "Desarrollador",
                  "descripcion": "Descripción",
                  "empresa": "Tech",
                  "caracteristicas": [
                    {
                      "competenciaId": 1,
                      "ponderacion": 5.0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/puestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.codigo").value("RECURSO_DUPLICADO"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe un puesto con el código 'DEV-01'"));
    }

    @Test
    @DisplayName("POST /api/puestos con regla de negocio violada (ej. competencias duplicadas) retorna HTTP 422 Unprocessable Entity")
    void crearPuesto_reglaNegocioViolada422() throws Exception {
        when(gestorPuesto.crearPuesto(any(PuestoDTO.class)))
                .thenThrow(new ReglaNegocioException("La competencia con ID 1 está repetida en el puesto"));

        String jsonRequest = """
                {
                  "codigo": "DEV-02",
                  "nombre": "Arquitecto",
                  "descripcion": "Descripción",
                  "empresa": "Tech",
                  "caracteristicas": [
                    {
                      "competenciaId": 1,
                      "ponderacion": 5.0
                    },
                    {
                      "competenciaId": 1,
                      "ponderacion": 8.0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/puestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"))
                .andExpect(jsonPath("$.mensaje").value("La competencia con ID 1 está repetida en el puesto"));
    }
}

