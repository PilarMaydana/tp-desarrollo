package ar.edu.utn.frsf.g9.capitalhumano.features.auth.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.AuthResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.CandidatoLoginRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.ConsultorLoginRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.service.AuthService;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la autenticación de usuarios (CU001: Autenticar Usuario).
 *
 * <p>Expone endpoints para la validación de credenciales tanto de Consultores (mediante LDAP)
 * como de Candidatos (mediante validación de cuestionario).</p>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para inicio de sesión y autenticación de usuarios (CU001)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Autentica a un usuario con perfil Consultor contra el servidor LDAP.
     *
     * @param request credenciales del consultor (usuario y contraseña)
     * @return token JWT, ID del usuario y rol CONSULTOR
     */
    @PostMapping("/consultor")
    @Operation(
            summary = "Autenticación de Consultores (LDAP)",
            description = "Valida las credenciales de un consultor contra el servicio LDAP institucional (Flujo Principal y Flujo Alternativo 5.A)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "El usuario o la contraseña no son válidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validación en los campos enviados",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<AuthResponseDTO> autenticarConsultor(@Valid @RequestBody ConsultorLoginRequestDTO request) {
        AuthResponseDTO response = authService.autenticarConsultor(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Autentica a un Candidato para acceder a la resolución de su cuestionario asignado.
     *
     * @param request datos del candidato (tipo y número de documento, clave de acceso)
     * @return token JWT, ID del cuestionario y rol CANDIDATO
     */
    @PostMapping("/candidato")
    @Operation(
            summary = "Autenticación de Candidatos (Cuestionario)",
            description = "Valida documento y clave de acceso del candidato contra cuestionarios activos o en proceso (Flujo Alternativo 3.A)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa para responder cuestionario",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Los datos ingresados no son válidos o no existe un cuestionario para el Candidato",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error de validación en los campos enviados",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<AuthResponseDTO> autenticarCandidato(@Valid @RequestBody CandidatoLoginRequestDTO request) {
        AuthResponseDTO response = authService.autenticarCandidato(request);
        return ResponseEntity.ok(response);
    }
}
