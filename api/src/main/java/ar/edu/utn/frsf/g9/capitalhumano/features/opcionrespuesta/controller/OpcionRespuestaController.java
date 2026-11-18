package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.OpcionRespuestaResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.gestor.GestorOpcionRespuesta;
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
import org.springframework.http.HttpStatus;
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
 * Controlador REST para la gestión y alta de Opciones de Respuesta (CU 024 / CU 029).
 */
@RestController
@RequestMapping("/api/opciones-respuesta")
@Tag(name = "Opciones de Respuesta", description = "Endpoints para la administración de Opciones de Respuesta (CU 024 / CU 029)")
public class OpcionRespuestaController {

    private final GestorOpcionRespuesta gestorOpcionRespuesta;

    public OpcionRespuestaController(GestorOpcionRespuesta gestorOpcionRespuesta) {
        this.gestorOpcionRespuesta = gestorOpcionRespuesta;
    }

    /**
     * Busca y lista opciones de respuesta aplicando criterio «contiene» sobre el nombre (CU 024).
     *
     * @param nombre filtro opcional por nombre de la opción de respuesta
     * @return lista de resúmenes de opciones de respuesta ordenada por nombre ascendente con HTTP 200 OK
     */
    @GetMapping
    @Operation(
            summary = "Buscar y listar opciones de respuesta",
            description = "Busca opciones de respuesta cuyo nombre contenga el texto indicado (sin distinguir mayúsculas/minúsculas). Si no se envía el parámetro 'nombre' o está en blanco, devuelve todas las opciones de respuesta. El resultado se retorna ordenado por nombre ascendente con datos de resumen (sin ítems)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de opciones de respuesta obtenido exitosamente (lista vacía si no hay coincidencias)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OpcionRespuestaResumenDTO.class)))
            )
    })
    public ResponseEntity<List<OpcionRespuestaResumenDTO>> buscarOpcionesRespuesta(
            @Parameter(description = "Texto opcional para filtrar por nombre con criterio «contiene»", example = "FRECUENCIA")
            @RequestParam(required = false) String nombre) {
        List<OpcionRespuestaResumenDTO> resultados = gestorOpcionRespuesta.buscarOpcionesRespuesta(nombre);
        return ResponseEntity.ok(resultados);
    }

    /**
     * Obtiene el detalle completo de una opción de respuesta por su ID, incluyendo sus ítems (CU 024 - Paso 5).
     *
     * @param id identificador único de la opción de respuesta
     * @return DTO de detalle de la opción de respuesta con sus ítems ordenados por orden ascendente y HTTP 200 OK
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener detalle de una opción de respuesta por ID",
            description = "Retorna los datos completos de una opción de respuesta junto con su lista de ítems ordenados por orden de visualización ascendente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Opción de respuesta obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = OpcionRespuestaDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Opción de respuesta no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<OpcionRespuestaDTO> obtenerOpcionRespuestaPorId(
            @Parameter(description = "Identificador único de la opción de respuesta", example = "1")
            @PathVariable Long id) {
        OpcionRespuestaDTO detalle = gestorOpcionRespuesta.obtenerPorId(id);
        return ResponseEntity.ok(detalle);
    }

    /**
     * Da de alta una nueva opción de respuesta con su lista de ítems (CU 029).
     *
     * @param dto datos de la opción de respuesta y sus ítems
     * @return DTO de la opción de respuesta creada con HTTP 201 Created
     */
    @PostMapping
    @Operation(
            summary = "Dar de alta una nueva opción de respuesta",
            description = "Registra un nuevo conjunto reutilizable de opciones de respuesta con su lista de ítems ordenados consecutivamente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Opción de respuesta creada exitosamente",
                    content = @Content(schema = @Schema(implementation = OpcionRespuestaDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos obligatorios incompletos o formato inválido (incluyendo validaciones de cada ítem)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe una opción de respuesta con el mismo nombre",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regla de negocio violada (orden de visualización no consecutivo o descripción de ítem duplicada)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<OpcionRespuestaDTO> crearOpcionRespuesta(@Valid @RequestBody OpcionRespuestaDTO dto) {
        OpcionRespuestaDTO creada = gestorOpcionRespuesta.crearOpcionRespuesta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}

