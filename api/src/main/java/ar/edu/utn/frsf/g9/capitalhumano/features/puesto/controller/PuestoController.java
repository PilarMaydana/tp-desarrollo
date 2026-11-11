package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto.PuestoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.gestor.GestorPuesto;
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
 * Controlador REST para el alta y administración de Puestos (CU 009).
 */
@RestController
@RequestMapping("/api/puestos")
@Tag(name = "Puestos", description = "Endpoints para la administración de Puestos de trabajo (CU 009)")
public class PuestoController {

    private final GestorPuesto gestorPuesto;

    public PuestoController(GestorPuesto gestorPuesto) {
        this.gestorPuesto = gestorPuesto;
    }

    /**
     * Da de alta un nuevo puesto en el sistema con sus características requeridas.
     *
     * @param dto datos de entrada del puesto y sus competencias asociadas
     * @return DTO del puesto creado con HTTP 201
     */
    @PostMapping
    @Operation(summary = "Dar de alta un nuevo puesto", description = "Registra un nuevo puesto en el sistema asociando las características y competencias evaluables.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Puesto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada incompletos o inválidos (ponderación fuera de rango o campos vacíos)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Código o nombre de puesto ya registrado en el sistema",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio violada (ej. competencias duplicadas en el puesto)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PuestoDTO> crearPuesto(@Valid @RequestBody PuestoDTO dto) {
        PuestoDTO creado = gestorPuesto.crearPuesto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}

