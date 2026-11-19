package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto.CandidatoResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor.GestorCandidatos;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CrearEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.DetallePuestoEvaluacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluacionCreadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PrevisualizacionEvaluacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PrevisualizarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.PuestoResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ValidacionCandidatosResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ValidarCandidatosRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.gestor.ExportadorExcelEvaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el flujo completo del CU 025 "Evaluar a Candidato".
 */
@RestController
@RequestMapping("/api/evaluaciones")
@Tag(name = "Evaluaciones", description = "Endpoints del wizard de Evaluación de Candidatos (CU 025)")
public class EvaluacionController {

    private final GestorCandidatos gestorCandidatos;

    public EvaluacionController(GestorCandidatos gestorCandidatos) {
        this.gestorCandidatos = gestorCandidatos;
    }

    @GetMapping("/candidatos")
    @Operation(
            summary = "Buscar candidatos activos",
            description = "Busca candidatos activos aplicando filtros opcionales 'contiene' (case-insensitive) por apellido, nombre y número de candidato (CU 025 - Pasos 2 y 3)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de candidatos coincidentes",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CandidatoResumenDTO.class)))
            )
    })
    public ResponseEntity<List<CandidatoResumenDTO>> buscarCandidatos(
            @Parameter(description = "Fragmento del apellido del candidato")
            @RequestParam(required = false) String apellido,
            @Parameter(description = "Fragmento del nombre del candidato")
            @RequestParam(required = false) String nombre,
            @Parameter(description = "Fragmento del número de candidato")
            @RequestParam(required = false) String nroCandidato) {
        return ResponseEntity.ok(gestorCandidatos.buscarCandidatos(apellido, nombre, nroCandidato));
    }

    @PostMapping("/validar-candidatos")
    @Operation(
            summary = "Validar candidatos seleccionados",
            description = "Valida que la selección no esté vacía y que ningún candidato posea un cuestionario en estado ACTIVO o EN_PROCESO (CU 025 - Pasos 4.A, 6 y 6.A)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todos los candidatos seleccionados son aptos para evaluar"),
            @ApiResponse(responseCode = "400", description = "Selección vacía o datos inválidos (Flujo 4.A)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Uno o más candidatos no existen o están eliminados",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Existen candidatos con cuestionarios ACTIVO o EN_PROCESO (Flujo 6.A)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ValidacionCandidatosResponseDTO> validarCandidatos(
            @Valid @RequestBody ValidarCandidatosRequestDTO request) {
        return ResponseEntity.ok(gestorCandidatos.verificarSinCuestionarioActivo(request.candidatoIds()));
    }

    @GetMapping("/puestos")
    @Operation(
            summary = "Listar puestos activos",
            description = "Retorna el listado de puestos o funciones activos disponibles para evaluar candidatos (CU 025 - Paso 8)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de puestos activos",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PuestoResumenDTO.class)))
            )
    })
    public ResponseEntity<List<PuestoResumenDTO>> listarPuestos() {
        return ResponseEntity.ok(gestorCandidatos.listarPuestos());
    }

    @GetMapping("/puestos/{puestoId}/competencias")
    @Operation(
            summary = "Obtener competencias y ponderaciones requeridas de un puesto",
            description = "Devuelve las competencias y ponderaciones requeridas del puesto validando que todas tengan factores activos con al menos 2 preguntas activas (CU 025 - Pasos 9 y 9.A)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Competencias evaluables del puesto"),
            @ApiResponse(responseCode = "404", description = "Puesto no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Una o más competencias no poseen factores válidos con al menos 2 preguntas activas (Flujo 9.A)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<DetallePuestoEvaluacionDTO> obtenerCompetenciasPuesto(
            @Parameter(description = "Identificador del puesto", example = "1")
            @PathVariable Long puestoId) {
        return ResponseEntity.ok(gestorCandidatos.obtenerDetallePuesto(puestoId));
    }

    @PostMapping("/previsualizar")
    @Operation(
            summary = "Previsualizar evaluación y claves generadas",
            description = "Valida puesto y candidatos, genera claves aleatorias de 8 caracteres y retorna el resumen de confirmación sin persistir en base de datos (CU 025 - Paso 11 y Flujo 12.A)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Previsualización generada correctamente"),
            @ApiResponse(responseCode = "400", description = "Campos obligatorios faltantes (Flujos 4.A / 10.A)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Puesto o candidato no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Candidatos bloqueados (6.A) o competencias no evaluables (9.A)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PrevisualizacionEvaluacionDTO> previsualizarEvaluacion(
            @Valid @RequestBody PrevisualizarEvaluacionRequestDTO request) {
        return ResponseEntity.ok(gestorCandidatos.previsualizarEvaluacion(request));
    }

    @PostMapping
    @Operation(
            summary = "Confirmar y crear evaluación",
            description = "Re-valida atómicamente puesto y candidatos, genera cuestionarios en estado ACTIVO con sus bloques y preguntas asignadas, y persiste la evaluación (CU 025 - Pasos 12 y 13)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evaluación y cuestionarios creados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Campos obligatorios faltantes o inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Puesto o candidato no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio violada (6.A, 9.A o clave inválida)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EvaluacionCreadaDTO> crearEvaluacion(
            @Valid @RequestBody CrearEvaluacionRequestDTO request) {
        EvaluacionCreadaDTO creada = gestorCandidatos.evaluarCandidatos(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/{evaluacionId}/exportar-excel")
    @Operation(
            summary = "Exportar evaluación a Excel (.xls)",
            description = "Genera y descarga un archivo Excel .xls con el listado de candidatos y sus claves de ingreso con formato aaaa-mm-dd-hh-mm-NombreFunción.xls (CU 025 - Pasos 14 a 17)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Archivo Excel .xls generado correctamente",
                    content = @Content(mediaType = ExportadorExcelEvaluacion.CONTENT_TYPE_XLS)
            ),
            @ApiResponse(responseCode = "404", description = "Evaluación no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<byte[]> exportarExcel(
            @Parameter(description = "Identificador de la evaluación creada", example = "1")
            @PathVariable Long evaluacionId) {
        ExportadorExcelEvaluacion.ArchivoExcelEvaluacion archivo = gestorCandidatos.exportarEvaluacionExcel(evaluacionId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ExportadorExcelEvaluacion.CONTENT_TYPE_XLS))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.nombreArchivo() + "\"")
                .body(archivo.contenido());
    }
}
