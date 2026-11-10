package ar.edu.utn.frsf.g9.capitalhumano.features.factor.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dto.FactorDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.gestor.GestorFactor;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el alta y consulta de Factores (CU 017).
 */
@RestController
@RequestMapping("/api/factores")
@Tag(name = "Factores", description = "Endpoints para la gestión y administración de Factores evaluables (CU 017)")
public class FactorController {

    private final GestorFactor gestorFactor;

    public FactorController(GestorFactor gestorFactor) {
        this.gestorFactor = gestorFactor;
    }

    /**
     * Da de alta un nuevo factor en el sistema (CU 017).
     *
     * @param dto datos de entrada del factor a crear
     * @return DTO del factor creado con HTTP 201 Created
     */
    @PostMapping
    @Operation(summary = "Dar de alta un nuevo factor", description = "Registra un nuevo factor asociado a una competencia existente con su número de orden.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Factor creado exitosamente",
                    content = @Content(schema = @Schema(implementation = FactorDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos obligatorios incompletos o formato inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Competencia referenciada no existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto por código, nombre o número de orden ya existente",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<FactorDTO> crearFactor(@Valid @RequestBody FactorDTO dto) {
        FactorDTO creado = gestorFactor.crearFactor(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Obtiene la lista de factores, con posibilidad de filtrar por ID de competencia.
     *
     * @param competenciaId ID opcional de la competencia para filtrar
     * @return lista de factores con HTTP 200 OK
     */
    @GetMapping
    @Operation(summary = "Listar factores", description = "Obtiene los factores registrados, opcionalmente filtrados por competencia.")
    public ResponseEntity<List<FactorDTO>> listarFactores(@RequestParam(required = false) Long competenciaId) {
        if (competenciaId != null) {
            return ResponseEntity.ok(gestorFactor.listarPorCompetencia(competenciaId));
        }
        return ResponseEntity.ok(gestorFactor.listarFactores());
    }
}
