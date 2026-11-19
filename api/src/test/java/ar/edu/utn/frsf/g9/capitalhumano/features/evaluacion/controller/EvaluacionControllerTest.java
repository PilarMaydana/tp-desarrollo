package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto.CandidatoResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor.GestorCandidatos;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CandidatoClaveDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CompetenciaRequeridaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CrearEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CuestionarioResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.DetallePuestoEvaluacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluacionCreadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PrevisualizacionEvaluacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PrevisualizarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PuestoResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ValidacionCandidatosResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.gestor.ExportadorExcelEvaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests MockMvc para {@link EvaluacionController} cubriendo éxitos y errores (400, 404, 422) del CU 025.
 */
@ExtendWith(MockitoExtension.class)
class EvaluacionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestorCandidatos gestorCandidatos;

    @BeforeEach
    void setUp() {
        EvaluacionController controller = new EvaluacionController(gestorCandidatos);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/evaluaciones/candidatos -> 200 OK con lista de candidatos")
    void buscarCandidatos_200() throws Exception {
        CandidatoResumenDTO ana = new CandidatoResumenDTO(4L, "4", "FERNÁNDEZ", "ANA", TipoDocumento.DNI, "30456789", "ana@example.com");
        when(gestorCandidatos.buscarCandidatos("fern", null, null)).thenReturn(List.of(ana));

        mockMvc.perform(get("/api/evaluaciones/candidatos").param("apellido", "fern"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].apellido").value("FERNÁNDEZ"))
                .andExpect(jsonPath("$[0].nombre").value("ANA"));
    }

    @Test
    @DisplayName("POST /api/evaluaciones/validar-candidatos con lista vacía -> 400 Bad Request con detalle (Flujo 4.A)")
    void validarCandidatos_listaVacia_400() throws Exception {
        mockMvc.perform(post("/api/evaluaciones/validar-candidatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"candidatoIds\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles", hasSize(1)))
                .andExpect(jsonPath("$.detalles[0].campo").value("candidatoIds"));

        verify(gestorCandidatos, never()).verificarSinCuestionarioActivo(any());
    }

    @Test
    @DisplayName("POST /api/evaluaciones/validar-candidatos con candidatos bloqueados -> 422 con detalles[] (Flujo 6.A)")
    void validarCandidatos_bloqueados_422() throws Exception {
        List<ReglaNegocioException.DetalleError> detalles = List.of(
                new ReglaNegocioException.DetalleError("candidato:1", "Nro. 1 - PÉREZ, JUAN (Estado: ACTIVO)")
        );
        when(gestorCandidatos.verificarSinCuestionarioActivo(List.of(1L)))
                .thenThrow(new ReglaNegocioException("Existen candidatos bloqueados", detalles));

        mockMvc.perform(post("/api/evaluaciones/validar-candidatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"candidatoIds\": [1]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"))
                .andExpect(jsonPath("$.detalles", hasSize(1)))
                .andExpect(jsonPath("$.detalles[0].campo").value("candidato:1"))
                .andExpect(jsonPath("$.detalles[0].mensaje").value("Nro. 1 - PÉREZ, JUAN (Estado: ACTIVO)"));
    }

    @Test
    @DisplayName("POST /api/evaluaciones/validar-candidatos exitoso -> 200 OK")
    void validarCandidatos_exitoso_200() throws Exception {
        CandidatoResumenDTO ana = new CandidatoResumenDTO(4L, "4", "FERNÁNDEZ", "ANA", TipoDocumento.DNI, "30456789", "ana@example.com");
        when(gestorCandidatos.verificarSinCuestionarioActivo(List.of(4L)))
                .thenReturn(new ValidacionCandidatosResponseDTO(true, 1, List.of(ana)));

        mockMvc.perform(post("/api/evaluaciones/validar-candidatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"candidatoIds\": [4]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.cantidadCandidatos").value(1));
    }

    @Test
    @DisplayName("GET /api/evaluaciones/puestos -> 200 OK")
    void listarPuestos_200() throws Exception {
        when(gestorCandidatos.listarPuestos())
                .thenReturn(List.of(new PuestoResumenDTO(1L, "DEV-01", "DESARROLLADOR SENIOR", "TECH SOLUTIONS")));

        mockMvc.perform(get("/api/evaluaciones/puestos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo").value("DEV-01"));
    }

    @Test
    @DisplayName("GET /api/evaluaciones/puestos/{id}/competencias -> 200 OK")
    void obtenerCompetenciasPuesto_200() throws Exception {
        DetallePuestoEvaluacionDTO dto = new DetallePuestoEvaluacionDTO(
                1L, "DEV-01", "DESARROLLADOR SENIOR", "TECH SOLUTIONS",
                List.of(new CompetenciaRequeridaDTO(1L, "COM-01", "COMUNICACIÓN", 8.0))
        );
        when(gestorCandidatos.obtenerDetallePuesto(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/evaluaciones/puestos/1/competencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puestoId").value(1))
                .andExpect(jsonPath("$.competencias", hasSize(1)))
                .andExpect(jsonPath("$.competencias[0].ponderacionRequerida").value(8.0));
    }

    @Test
    @DisplayName("GET /api/evaluaciones/puestos/{id}/competencias con puesto inexistente -> 404")
    void obtenerCompetenciasPuesto_404() throws Exception {
        when(gestorCandidatos.obtenerDetallePuesto(99L))
                .thenThrow(new RecursoNoEncontradoException("Puesto", 99L));

        mockMvc.perform(get("/api/evaluaciones/puestos/99/competencias"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    @DisplayName("GET /api/evaluaciones/puestos/{id}/competencias con competencia no evaluable -> 422 con detalles[] (Flujo 9.A)")
    void obtenerCompetenciasPuesto_flujo9A_422() throws Exception {
        when(gestorCandidatos.obtenerDetallePuesto(2L))
                .thenThrow(new ReglaNegocioException(
                        "Competencias no evaluables",
                        List.of(new ReglaNegocioException.DetalleError("competencia", "RESOLUCIÓN DE PROBLEMAS"))
                ));

        mockMvc.perform(get("/api/evaluaciones/puestos/2/competencias"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"))
                .andExpect(jsonPath("$.detalles", hasSize(1)))
                .andExpect(jsonPath("$.detalles[0].mensaje").value("RESOLUCIÓN DE PROBLEMAS"));
    }

    @Test
    @DisplayName("POST /api/evaluaciones/previsualizar con múltiples omisiones -> 400 con TODOS los detalles (Flujos 4.A y 10.A)")
    void previsualizar_camposFaltantes_400ConTodosLosDetalles() throws Exception {
        mockMvc.perform(post("/api/evaluaciones/previsualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puestoId\": null, \"candidatoIds\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles", hasSize(2)))
                .andExpect(jsonPath("$.detalles[*].campo", containsInAnyOrder("puestoId", "candidatoIds")));

        verify(gestorCandidatos, never()).previsualizarEvaluacion(any());
    }

    @Test
    @DisplayName("POST /api/evaluaciones/previsualizar exitoso -> 200 OK")
    void previsualizar_200() throws Exception {
        PrevisualizacionEvaluacionDTO preview = new PrevisualizacionEvaluacionDTO(
                1L, "DEV-01", "DESARROLLADOR SENIOR", "TECH SOLUTIONS", 60, 5,
                List.of(new CandidatoClaveDTO(4L, "4", "FERNÁNDEZ", "ANA", TipoDocumento.DNI, "30456789", "A7K9M2P4"))
        );
        when(gestorCandidatos.previsualizarEvaluacion(any(PrevisualizarEvaluacionRequestDTO.class))).thenReturn(preview);

        mockMvc.perform(post("/api/evaluaciones/previsualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puestoId\": 1, \"candidatoIds\": [4]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puestoId").value(1))
                .andExpect(jsonPath("$.candidatos[0].claveAcceso").value("A7K9M2P4"));
    }

    @Test
    @DisplayName("POST /api/evaluaciones exitoso -> 201 Created")
    void crearEvaluacion_201() throws Exception {
        EvaluacionCreadaDTO creada = new EvaluacionCreadaDTO(
                2L, 1L, "DEV-01", "DESARROLLADOR SENIOR", "TECH SOLUTIONS",
                LocalDateTime.of(2026, 11, 19, 13, 0), 60, 5,
                List.of(new CuestionarioResumenDTO(
                        6L, 4L, "4", "FERNÁNDEZ", "ANA", TipoDocumento.DNI, "30456789",
                        "A7K9M2P4", EstadoCuestionario.ACTIVO, 2, 6
                ))
        );
        when(gestorCandidatos.evaluarCandidatos(any(CrearEvaluacionRequestDTO.class))).thenReturn(creada);

        mockMvc.perform(post("/api/evaluaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puestoId\": 1, \"candidatoIds\": [4]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.evaluacionId").value(2))
                .andExpect(jsonPath("$.cuestionarios[0].estado").value("ACTIVO"))
                .andExpect(jsonPath("$.cuestionarios[0].claveAcceso").value("A7K9M2P4"));
    }

    @Test
    @DisplayName("POST /api/evaluaciones con múltiples omisiones -> 400 con todos los detalles")
    void crearEvaluacion_camposVacios_400() throws Exception {
        mockMvc.perform(post("/api/evaluaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles", hasSize(2)))
                .andExpect(jsonPath("$.detalles[*].campo", containsInAnyOrder("puestoId", "candidatoIds")));
    }

    @Test
    @DisplayName("GET /api/evaluaciones/{id}/exportar-excel -> 200 OK con headers de descarga .xls")
    void exportarExcel_200() throws Exception {
        byte[] bytesSimulados = new byte[]{1, 2, 3, 4};
        when(gestorCandidatos.exportarEvaluacionExcel(2L))
                .thenReturn(new ExportadorExcelEvaluacion.ArchivoExcelEvaluacion(
                        "2026-11-19-13-00-DESARROLLADOR SENIOR.xls",
                        bytesSimulados
                ));

        mockMvc.perform(get("/api/evaluaciones/2/exportar-excel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.ms-excel"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"2026-11-19-13-00-DESARROLLADOR SENIOR.xls\""))
                .andExpect(content().bytes(bytesSimulados));
    }

    @Test
    @DisplayName("GET /api/evaluaciones/{id}/exportar-excel inexistente -> 404")
    void exportarExcel_404() throws Exception {
        when(gestorCandidatos.exportarEvaluacionExcel(999L))
                .thenThrow(new RecursoNoEncontradoException("Evaluacion", 999L));

        mockMvc.perform(get("/api/evaluaciones/999/exportar-excel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }
}
