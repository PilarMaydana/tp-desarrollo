package ar.edu.utn.frsf.g9.capitalhumano.features.reporte.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.EvaluacionOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.PuestoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.dto.ReporteOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.reporte.gestor.GestorReportes;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el CU 028 "Emitir Orden de mérito".
 */
@RestController
@RequestMapping("/api/orden-merito")
@Tag(name = "Orden de Mérito", description = "Endpoints para la emisión del reporte de Orden de Mérito (CU 028)")
public class OrdenMeritoController {

    private final GestorReportes gestorReportes;

    public OrdenMeritoController(GestorReportes gestorReportes) {
        this.gestorReportes = gestorReportes;
    }

    @GetMapping("/puestos")
    @Operation(
            summary = "Buscar puestos para emitir orden de mérito",
            description = "Busca puestos activos con criterio «contiene» (case-insensitive) sobre código, nombre y/o empresa. Exige al menos un criterio informado (CU 028 - Pasos 2 a 4)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de puestos coincidentes ordenados por nombre (puede ser vacío)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PuestoOrdenMeritoDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se ingresó ningún criterio de búsqueda (código VALIDACION)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<List<PuestoOrdenMeritoDTO>> buscarPuestos(
            @Parameter(description = "Fragmento del código de puesto (contiene, case-insensitive)", example = "ING")
            @RequestParam(required = false) String codigo,
            @Parameter(description = "Fragmento del nombre del puesto (contiene, case-insensitive)", example = "INGENIERO")
            @RequestParam(required = false) String nombre,
            @Parameter(description = "Fragmento del nombre de la empresa (contiene, case-insensitive)", example = "DATA")
            @RequestParam(required = false) String empresa) {
        return ResponseEntity.ok(gestorReportes.buscarPuestos(codigo, nombre, empresa));
    }

    @GetMapping("/puestos/{puestoId}/evaluaciones")
    @Operation(
            summary = "Listar evaluaciones de un puesto",
            description = "Devuelve las evaluaciones del puesto seleccionado ordenadas de más reciente a más antigua para elegir cuáles incluir en el orden de mérito (CU 028 - Paso 6 y Flujo 6.A)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de evaluaciones del puesto",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EvaluacionOrdenMeritoDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El puesto indicado no existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "El puesto no posee evaluaciones asociadas (Flujo 6.A)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<List<EvaluacionOrdenMeritoDTO>> listarEvaluaciones(
            @Parameter(description = "Identificador del puesto", example = "4")
            @PathVariable Long puestoId) {
        return ResponseEntity.ok(gestorReportes.listarEvaluacionesPorPuesto(puestoId));
    }

    @GetMapping("/puestos/{puestoId}")
    @Operation(
            summary = "Emitir reporte de orden de mérito",
            description = "Emite el listado de orden de mérito para el puesto seleccionado, incluyendo todas sus evaluaciones si evaluacionIds se omite o viene vacío, o solo el subconjunto indicado (CU 028 - Paso 7 y Flujo 6.A)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte de orden de mérito emitido correctamente",
                    content = @Content(schema = @Schema(implementation = ReporteOrdenMeritoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El puesto indicado no existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "El puesto no posee evaluaciones (Flujo 6.A) o alguna evaluación solicitada no pertenece al puesto",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ReporteOrdenMeritoDTO> emitirOrdenDeMerito(
            @Parameter(description = "Identificador del puesto", example = "4")
            @PathVariable Long puestoId,
            @Parameter(description = "Lista opcional de IDs de evaluación a incluir (separados por coma). Si se omite o queda vacía, se incluyen todas.", example = "3,4")
            @RequestParam(required = false) List<Long> evaluacionIds) {
        return ResponseEntity.ok(gestorReportes.emitirOrdenDeMerito(puestoId, evaluacionIds));
    }
}
