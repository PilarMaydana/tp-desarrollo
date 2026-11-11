package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaFiltroDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor.GestorPregunta;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión de Preguntas (CU 020 - Gestionar Preguntas).
 */
@RestController
@RequestMapping("/api/preguntas")
@Tag(name = "Preguntas", description = "Endpoints para la gestión, búsqueda, alta, modificación y baja de Preguntas (CU 020)")
public class PreguntaController {

    private final GestorPregunta gestorPregunta;

    public PreguntaController(GestorPregunta gestorPregunta) {
        this.gestorPregunta = gestorPregunta;
    }

    /**
     * Búsqueda de preguntas según criterios ingresados (CU 020 - Flujo Principal).
     * Requiere al menos un criterio de búsqueda (competencia, factor o nombre).
     *
     * @param competencia criterio opcional de competencia
     * @param factor criterio opcional de factor
     * @param nombre criterio opcional de nombre o contenido de pregunta
     * @return lista de preguntas coincidentes
     */
    @GetMapping
    @Operation(summary = "Buscar preguntas", description = "Busca preguntas con coincidencia parcial 'contiene' en competencia, factor o nombre.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preguntas encontradas exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PreguntaDTO.class)))),
            @ApiResponse(responseCode = "400", description = "No se envió ningún criterio de búsqueda",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<PreguntaDTO>> buscarPreguntas(
            @RequestParam(required = false) String competencia,
            @RequestParam(required = false) String factor,
            @RequestParam(required = false) String nombre) {
        PreguntaFiltroDTO filtro = new PreguntaFiltroDTO(competencia, factor, nombre);
        List<PreguntaDTO> resultados = gestorPregunta.buscarPreguntas(filtro);
        return ResponseEntity.ok(resultados);
    }

    /**
     * Obtiene los datos detallados de una pregunta por su ID.
     *
     * @param id identificador de la pregunta
     * @return DTO de la pregunta
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener pregunta por ID", description = "Obtiene los detalles de una pregunta activa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pregunta obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PreguntaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pregunta no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PreguntaDTO> obtenerPregunta(@PathVariable Long id) {
        return ResponseEntity.ok(gestorPregunta.obtenerPorId(id));
    }

    /**
     * Da de alta una nueva pregunta con sus opciones (CU 021 - Flujo 3.A).
     *
     * @param dto datos de la pregunta
     * @return pregunta creada con HTTP 201 Created
     */
    @PostMapping
    @Operation(summary = "Alta de pregunta", description = "Crea una nueva pregunta asociada a un factor existente con sus opciones de respuesta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pregunta creada exitosamente",
                    content = @Content(schema = @Schema(implementation = PreguntaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos obligatorios incompletos o menos de 2 opciones",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Factor referenciado no existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Pregunta duplicada en nombre o enunciado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PreguntaDTO> crearPregunta(@Valid @RequestBody PreguntaDTO dto) {
        PreguntaDTO creada = gestorPregunta.crearPregunta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Modifica una pregunta existente (CU 022 - Flujo 5.A).
     *
     * @param id identificador de la pregunta a modificar
     * @param dto nuevos datos de la pregunta
     * @return pregunta modificada con HTTP 200 OK
     */
    @PutMapping("/{id}")
    @Operation(summary = "Modificar pregunta", description = "Modifica los datos y opciones de una pregunta existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pregunta modificada exitosamente",
                    content = @Content(schema = @Schema(implementation = PreguntaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos obligatorios incompletos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Pregunta no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto por duplicidad de nombre o enunciado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PreguntaDTO> modificarPregunta(
            @PathVariable Long id,
            @Valid @RequestBody PreguntaDTO dto) {
        PreguntaDTO modificada = gestorPregunta.modificarPregunta(id, dto);
        return ResponseEntity.ok(modificada);
    }

    /**
     * Da de baja lógica una pregunta (CU 023 - Flujo 5.B).
     *
     * @param id identificador de la pregunta a eliminar
     * @return respuesta vacía HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja pregunta", description = "Aplica una baja lógica a la pregunta registrando auditoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Baja lógica aplicada correctamente"),
            @ApiResponse(responseCode = "404", description = "Pregunta no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> eliminarPregunta(@PathVariable Long id) {
        gestorPregunta.eliminarPregunta(id);
        return ResponseEntity.noContent().build();
    }
}

