package ar.edu.utn.frsf.g9.capitalhumano.features.competencia.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dto.CompetenciaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.gestor.GestorCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dto.FactorDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor.GestorPregunta;
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
 * Tests unitarios de {@link CompetenciaController} utilizando MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class CompetenciaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestorCompetencia gestorCompetencia;

    @Mock
    private GestorPregunta gestorPregunta;

    @BeforeEach
    void setUp() {
        CompetenciaController controller = new CompetenciaController(gestorCompetencia, gestorPregunta);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/competencias con datos válidos retorna 201 Created y el DTO creado")
    void crearCompetencia_exitoso201() throws Exception {
        CompetenciaDTO respuesta = new CompetenciaDTO(
                1L,
                "COM-01",
                "COMUNICACIÓN",
                "CAPACIDAD DE TRANSMITIR IDEAS"
        );

        when(gestorCompetencia.crearCompetencia(any(CompetenciaDTO.class))).thenReturn(respuesta);

        String jsonRequest = """
                {
                  "codigo": "COM-01",
                  "nombre": "Comunicación",
                  "descripcion": "Capacidad de transmitir ideas"
                }
                """;

        mockMvc.perform(post("/api/competencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("COM-01"))
                .andExpect(jsonPath("$.nombre").value("COMUNICACIÓN"))
                .andExpect(jsonPath("$.descripcion").value("CAPACIDAD DE TRANSMITIR IDEAS"));
    }

    @Test
    @DisplayName("POST /api/competencias con campos vacíos retorna 400 Bad Request con todos los detalles por campo")
    void crearCompetencia_camposVacios400ConTodosLosDetalles() throws Exception {
        String jsonRequest = """
                {
                  "codigo": "",
                  "nombre": "   ",
                  "descripcion": null
                }
                """;

        mockMvc.perform(post("/api/competencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.path").value("/api/competencias"))
                .andExpect(jsonPath("$.detalles", hasSize(3)))
                .andExpect(jsonPath("$.detalles[*].campo",
                        containsInAnyOrder("codigo", "nombre", "descripcion")));

        verify(gestorCompetencia, never()).crearCompetencia(any());
    }

    @Test
    @DisplayName("GET /api/competencias retorna 200 OK y la lista de competencias")
    void listarCompetencias_exitoso200() throws Exception {
        CompetenciaDTO c1 = new CompetenciaDTO(1L, "COM-01", "COMUNICACIÓN", "CAPACIDAD DE TRANSMITIR IDEAS");
        when(gestorCompetencia.listarCompetencias()).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/competencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].codigo").value("COM-01"))
                .andExpect(jsonPath("$[0].nombre").value("COMUNICACIÓN"));
    }

    @Test
    @DisplayName("GET /api/competencias/{codigoCompetencia}/factores retorna 200 OK y la lista de factores")
    void listarFactoresPorCompetencia_exitoso200() throws Exception {
        FactorDTO f1 = new FactorDTO(1L, "FAC-01", "COMUNICACIÓN ORAL", "DESCRIPCION", 1L, "COMUNICACIÓN", "COM-01", 1);
        FactorDTO f2 = new FactorDTO(2L, "FAC-02", "COMUNICACIÓN ESCRITA", "DESCRIPCION 2", 1L, "COMUNICACIÓN", "COM-01", 2);

        when(gestorPregunta.listarFactoresPorCompetencia("COM-01")).thenReturn(List.of(f1, f2));

        mockMvc.perform(get("/api/competencias/COM-01/factores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].codigo").value("FAC-01"))
                .andExpect(jsonPath("$[0].nroOrden").value(1))
                .andExpect(jsonPath("$[1].codigo").value("FAC-02"))
                .andExpect(jsonPath("$[1].nroOrden").value(2));

        verify(gestorPregunta).listarFactoresPorCompetencia("COM-01");
    }

    @Test
    @DisplayName("GET /api/competencias/{codigoCompetencia}/factores con competencia inexistente retorna 404 Not Found")
    void listarFactoresPorCompetencia_competenciaInexistente404() throws Exception {
        when(gestorPregunta.listarFactoresPorCompetencia("NO-EXISTE"))
                .thenThrow(new RecursoNoEncontradoException("Competencia con código 'NO-EXISTE' no encontrada"));

        mockMvc.perform(get("/api/competencias/NO-EXISTE/factores"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }
}
