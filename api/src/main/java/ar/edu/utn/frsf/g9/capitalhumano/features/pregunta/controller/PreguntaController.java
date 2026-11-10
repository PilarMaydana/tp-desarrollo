package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto.PreguntaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.gestor.GestorPregunta;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el alta y administración de Preguntas (CU 021).
 */
@RestController
@RequestMapping("/api/preguntas")
@Tag(name = "Preguntas", description = "Endpoints para la gestión y alta de Preguntas (CU 021)")
public class PreguntaController {

    private final GestorPregunta gestorPregunta;

    public PreguntaController(GestorPregunta gestorPregunta) {
        this.gestorPregunta = gestorPregunta;
    }

    /**
     * Da de alta una nueva pregunta en el sistema con sus opciones de respuesta (CU 021).
     *
     * @param dto datos de la pregunta y sus opciones a registrar
     * @return DTO de la pregunta creada con HTTP 201
     */
    @PostMapping
    @Operation(summary = "Dar de alta una nueva pregunta", description = "Registra una nueva pregunta en el banco asociada a un factor con sus opciones de respuesta ponderadas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pregunta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada incompletos o inválidos (omisiones o longitud excedida)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Factor referenciado no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Pregunta ya existente para el factor (nombre o enunciado duplicado)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio violada (ej. suma de ponderaciones distinta de 10 o valores negativos)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PreguntaDTO> crearPregunta(@Valid @RequestBody PreguntaDTO dto) {
        PreguntaDTO creada = gestorPregunta.crearPregunta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
