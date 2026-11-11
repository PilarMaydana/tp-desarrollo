package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto.OpcionRespuestaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.gestor.GestorOpcionRespuesta;
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
 * Controlador REST para el alta de Opciones de Respuesta (CU 029).
 */
@RestController
@RequestMapping("/api/opciones-respuesta")
@Tag(name = "Opciones de Respuesta", description = "Endpoints para la administración de Opciones de Respuesta (CU 029)")
public class OpcionRespuestaController {

    private final GestorOpcionRespuesta gestorOpcionRespuesta;

    public OpcionRespuestaController(GestorOpcionRespuesta gestorOpcionRespuesta) {
        this.gestorOpcionRespuesta = gestorOpcionRespuesta;
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

