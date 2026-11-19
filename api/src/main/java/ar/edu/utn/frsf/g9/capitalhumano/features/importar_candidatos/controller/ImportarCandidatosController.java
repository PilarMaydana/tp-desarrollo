package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.ErrorImportacionDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.ImportacionCandidatosResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.service.ImportarCandidatosService;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST para el Caso de Uso 006: Importar Candidatos.
 */
@RestController
@RequestMapping("/api/candidatos")
@Tag(name = "Candidatos", description = "Endpoints para la gestión e importación de candidatos (CU006)")
public class ImportarCandidatosController {

    private final ImportarCandidatosService importarCandidatosService;

    public ImportarCandidatosController(ImportarCandidatosService importarCandidatosService) {
        this.importarCandidatosService = importarCandidatosService;
    }

    /**
     * Importa candidatos a partir de un archivo CSV en formato multipart/form-data.
     *
     * @param file archivo .csv con delimitador ';' y encabezados requeridos
     * @return estadísticas con cantidad de candidatos creados, actualizados y eliminados
     */
    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Importar candidatos desde archivo CSV",
            description = "Procesa un archivo CSV delimitado por ';' con datos de candidatos. Realiza altas, actualizaciones y bajas lógicas atómicas.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Importación realizada exitosamente",
                    content = @Content(schema = @Schema(implementation = ImportacionCandidatosResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Archivo inválido, formato incorrecto, delimitador erróneo o errores de validación en filas",
                    content = @Content(schema = @Schema(implementation = ErrorImportacionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado en el sistema",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Requiere rol CONSULTOR",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ImportacionCandidatosResponseDTO> importarCandidatos(
            @Parameter(description = "Archivo .csv que contiene los datos de los candidatos a importar", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        ImportacionCandidatosResponseDTO resultado = importarCandidatosService.importarCandidatos(file);
        return ResponseEntity.ok(resultado);
    }
}
