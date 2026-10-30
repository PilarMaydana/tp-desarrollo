package ar.edu.utn.frsf.g9.capitalhumano.features.competencia.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dto.CompetenciaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.gestor.GestorCompetencia;
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
 * Tests unitarios de {@link CompetenciaController} utilizando MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class CompetenciaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestorCompetencia gestorCompetencia;

    @BeforeEach
    void setUp() {
        CompetenciaController controller = new CompetenciaController(gestorCompetencia);
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
}
