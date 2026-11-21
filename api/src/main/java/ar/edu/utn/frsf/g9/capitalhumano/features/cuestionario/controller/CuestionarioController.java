package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioIniciadoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.InstruccionesCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.ResponderBloqueRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.ResponderBloqueResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el CU 026 "Completar Cuestionario" (instrucciones,
 * validación de disponibilidad, inicio/reingreso, respuesta de bloques y cierre).
 */
@RestController
@RequestMapping("/api/cuestionarios")
@Tag(name = "Cuestionarios", description = "Endpoints para la realización de cuestionarios por candidatos (CU 026)")
public class CuestionarioController {

    private final GestorCuestionario gestorCuestionario;

    public CuestionarioController(GestorCuestionario gestorCuestionario) {
        this.gestorCuestionario = gestorCuestionario;
    }

    @GetMapping("/{id}/instrucciones")
    @Operation(
            summary = "Obtener instrucciones del cuestionario",
            description = "Valida la disponibilidad del cuestionario del candidato actual (sin incrementar accesos) y devuelve las instrucciones generales junto con el puesto y tiempo máximo (CU 026 - Paso 1)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Instrucciones y metadatos del cuestionario disponibles",
                    content = @Content(schema = @Schema(implementation = InstruccionesCuestionarioDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El cuestionario no existe o no pertenece al candidato actual",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "El cuestionario no está disponible o superó el tiempo máximo permitido (Flujos 1.A / 1.B)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<InstruccionesCuestionarioDTO> obtenerInstrucciones(
            @Parameter(description = "Identificador del cuestionario", example = "6")
            @PathVariable Long id) {
        return ResponseEntity.ok(gestorCuestionario.obtenerInstrucciones(id));
    }

    @PostMapping("/{id}/iniciar")
    @Operation(
            summary = "Iniciar o reingresar al cuestionario",
            description = "Valida disponibilidad y límite de accesos, incrementa la cantidad de accesos, pasa el cuestionario a EN_PROCESO si estaba ACTIVO (conservando bloques generados por CU 025 o armándolos si faltan) y devuelve el primer bloque pendiente de responder (CU 026 - Pasos 2 a 4 y Flujo 1.C)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cuestionario iniciado o reingresado correctamente con el bloque actual",
                    content = @Content(schema = @Schema(implementation = CuestionarioIniciadoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El cuestionario no existe o no pertenece al candidato actual",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "El cuestionario no está disponible, venció (Flujos 1.A / 1.B), superó el límite de accesos o no tiene factores válidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<CuestionarioIniciadoDTO> iniciarCuestionario(
            @Parameter(description = "Identificador del cuestionario", example = "6")
            @PathVariable Long id) {
        return ResponseEntity.ok(gestorCuestionario.iniciarCuestionario(id));
    }

    @PostMapping("/{id}/bloques/{numero}/respuestas")
    @Operation(
            summary = "Responder un bloque del cuestionario",
            description = "Valida disponibilidad y vigencia de tiempo, estado EN_PROCESO, pertenencia y completitud de las respuestas del bloque actual (Flujo 6.A). Si quedan bloques devuelve el siguiente (Flujo 8.A); si es el último calcula y persiste resultados y cierra el cuestionario en estado COMPLETO sin exponer puntajes (CU 026 - Pasos 5 a 8)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bloque guardado correctamente (avanza al siguiente bloque o finaliza el cuestionario)",
                    content = @Content(schema = @Schema(implementation = ResponderBloqueResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Faltan responder una o más preguntas del bloque (Flujo 6.A, código VALIDACION con detalles por pregunta) o cuerpo inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El cuestionario no existe, pertenece a otro candidato o el bloque indicado no existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "El cuestionario venció (pasa a INCOMPLETO), no está EN_PROCESO, el bloque ya fue respondido o no es el actual, o alguna pregunta/opción no corresponde",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ResponderBloqueResponseDTO> responderBloque(
            @Parameter(description = "Identificador del cuestionario", example = "12")
            @PathVariable Long id,
            @Parameter(description = "Número secuencial del bloque a responder (1..N)", example = "1")
            @PathVariable Integer numero,
            @Valid @RequestBody ResponderBloqueRequestDTO request) {
        return ResponseEntity.ok(gestorCuestionario.responderBloque(id, numero, request));
    }
}
