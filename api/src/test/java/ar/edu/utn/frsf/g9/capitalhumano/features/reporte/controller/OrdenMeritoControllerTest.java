package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.CompetenciaNoAlcanzadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.CompetenciaRequeridaReporteDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.DatosAdicionalesDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.EvaluacionIncluidaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.EvaluacionOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaCandidatoReporteDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaNoAlcanzoMinimoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.FilaOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.PuestoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.PuestoReporteDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.ReporteOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.TotalesReporteDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.gestor.GestorReportes;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests MockMvc para {@link OrdenMeritoController} cubriendo 200, 400, 404 y 422 de cada endpoint.
 */
@ExtendWith(MockitoExtension.class)
class OrdenMeritoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestorReportes gestorReportes;

    @BeforeEach
    void setUp() {
        OrdenMeritoController controller = new OrdenMeritoController(gestorReportes);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/orden-merito/puestos -> 200 OK con puestos coincidentes")
    void buscarPuestos_200() throws Exception {
        when(gestorReportes.buscarPuestos(null, "ingeniero", null)).thenReturn(List.of(
                new PuestoOrdenMeritoDTO(4L, "ING-DATA-01", "INGENIERO DE DATOS", "DATA CORP SA", 10L, 1L, 2L),
                new PuestoOrdenMeritoDTO(5L, "ING-CLOUD-01", "INGENIERO DE PLATAFORMA CLOUD", "NUBE SUR SRL", 0L, 0L, 0L)
        ));

        mockMvc.perform(get("/api/orden-merito/puestos").param("nombre", "ingeniero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].puestoId").value(4))
                .andExpect(jsonPath("$[0].codigo").value("ING-DATA-01"))
                .andExpect(jsonPath("$[0].cantidadCandidatos").value(10))
                .andExpect(jsonPath("$[0].evaluacionesCompletas").value(1))
                .andExpect(jsonPath("$[0].evaluacionesTotal").value(2));
    }

    @Test
    @DisplayName("GET /api/orden-merito/puestos sin criterios -> 400 VALIDACION")
    void buscarPuestos_400() throws Exception {
        when(gestorReportes.buscarPuestos(null, null, null))
                .thenThrow(new ValidacionException(
                        "Debe ingresar al menos un criterio de búsqueda (código, nombre o empresa).",
                        List.of(new ValidacionException.DetalleError("criterios", "Ingrese al menos un criterio."))
                ));

        mockMvc.perform(get("/api/orden-merito/puestos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/orden-merito/puestos/{puestoId}/evaluaciones -> 200 OK")
    void listarEvaluaciones_200() throws Exception {
        when(gestorReportes.listarEvaluacionesPorPuesto(4L)).thenReturn(List.of(
                new EvaluacionOrdenMeritoDTO(4L, LocalDateTime.of(2026, 11, 21, 10, 0), 7L, 3L),
                new EvaluacionOrdenMeritoDTO(3L, LocalDateTime.of(2026, 11, 13, 10, 0), 3L, 3L)
        ));

        mockMvc.perform(get("/api/orden-merito/puestos/4/evaluaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].evaluacionId").value(4))
                .andExpect(jsonPath("$[0].cantidadCandidatos").value(7))
                .andExpect(jsonPath("$[0].cantidadCompletos").value(3));
    }

    @Test
    @DisplayName("GET /api/orden-merito/puestos/{puestoId}/evaluaciones -> 404 Not Found")
    void listarEvaluaciones_404() throws Exception {
        when(gestorReportes.listarEvaluacionesPorPuesto(999L))
                .thenThrow(new RecursoNoEncontradoException("Puesto", 999L));

        mockMvc.perform(get("/api/orden-merito/puestos/999/evaluaciones"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    @DisplayName("GET /api/orden-merito/puestos/{puestoId}/evaluaciones -> 422 Flujo 6.A")
    void listarEvaluaciones_422() throws Exception {
        when(gestorReportes.listarEvaluacionesPorPuesto(5L))
                .thenThrow(new ReglaNegocioException("El puesto seleccionado no posee evaluaciones asociadas."));

        mockMvc.perform(get("/api/orden-merito/puestos/5/evaluaciones"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"));
    }

    @Test
    @DisplayName("GET /api/orden-merito/puestos/{puestoId} -> 200 OK con reporte completo")
    void emitirReporte_200() throws Exception {
        LocalDateTime emision = LocalDateTime.of(2026, 11, 23, 19, 0);
        ReporteOrdenMeritoDTO dto = new ReporteOrdenMeritoDTO(
                new PuestoReporteDTO(4L, "ING-DATA-01", "INGENIERO DE DATOS", "DATA CORP SA"),
                List.of(new EvaluacionIncluidaDTO(3L, emision.minusDays(10))),
                emision,
                List.of(new CompetenciaRequeridaReporteDTO(1L, "COM-01", "COMUNICACIÓN", new BigDecimal("7.0"))),
                List.of(new FilaOrdenMeritoDTO(
                        1, 13L, 3L, TipoDocumento.DNI, "35100001", "ALVAREZ", "VALENTINA",
                        new BigDecimal("9.50"),
                        DatosAdicionalesDTO.paraCompleto(emision.minusDays(9), emision.minusDays(9).plusMinutes(35), 1)
                )),
                List.of(new FilaNoAlcanzoMinimoDTO(
                        17L, 4L, TipoDocumento.DNI, "35100005", "ESPINOZA", "TOMAS",
                        new BigDecimal("7.50"),
                        DatosAdicionalesDTO.paraCompleto(emision.minusHours(38), emision.minusHours(37), 2),
                        List.of(new CompetenciaNoAlcanzadaDTO(5L, "TEC-01", "COMPETENCIAS TÉCNICAS",
                                new BigDecimal("6.50"), new BigDecimal("8.0")))
                )),
                List.of(new FilaCandidatoReporteDTO(
                        19L, 4L, TipoDocumento.DNI, "35100007", "GARCIA", "NICOLAS",
                        null,
                        DatosAdicionalesDTO.paraNoCompleto(emision.minusHours(36), emision.minusHours(35), 2)
                )),
                List.of(),
                List.of(),
                List.of(),
                new TotalesReporteDTO(1, 1, 1, 0, 0, 0, 3)
        );
        when(gestorReportes.emitirOrdenDeMerito(4L, List.of(3L, 4L))).thenReturn(dto);

        mockMvc.perform(get("/api/orden-merito/puestos/4").param("evaluacionIds", "3,4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puesto.codigo").value("ING-DATA-01"))
                .andExpect(jsonPath("$.ordenDeMerito", hasSize(1)))
                .andExpect(jsonPath("$.ordenDeMerito[0].posicion").value(1))
                .andExpect(jsonPath("$.ordenDeMerito[0].puntajeObtenido").value(9.50))
                .andExpect(jsonPath("$.ordenDeMerito[0].datosAdicionales.ultimoIngreso").value(nullValue()))
                .andExpect(jsonPath("$.noAlcanzaronMinimos", hasSize(1)))
                .andExpect(jsonPath("$.noAlcanzaronMinimos[0].competenciasNoAlcanzadas", hasSize(1)))
                .andExpect(jsonPath("$.incompletos", hasSize(1)))
                .andExpect(jsonPath("$.incompletos[0].puntajeObtenido").value(nullValue()))
                .andExpect(jsonPath("$.incompletos[0].datosAdicionales.fechaFin").value(nullValue()))
                .andExpect(jsonPath("$.totales.totalGeneral").value(3));
    }

    @Test
    @DisplayName("GET /api/orden-merito/puestos/{puestoId} -> 404 Not Found")
    void emitirReporte_404() throws Exception {
        when(gestorReportes.emitirOrdenDeMerito(999L, null))
                .thenThrow(new RecursoNoEncontradoException("Puesto", 999L));

        mockMvc.perform(get("/api/orden-merito/puestos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    @DisplayName("GET /api/orden-merito/puestos/{puestoId} -> 422 evaluación ajena o puesto sin evaluaciones")
    void emitirReporte_422() throws Exception {
        when(gestorReportes.emitirOrdenDeMerito(4L, List.of(1L)))
                .thenThrow(new ReglaNegocioException("Una o más evaluaciones seleccionadas no pertenecen al puesto indicado."));

        mockMvc.perform(get("/api/orden-merito/puestos/4").param("evaluacionIds", "1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"));
    }
}
