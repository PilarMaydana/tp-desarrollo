package ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta para una autenticación exitosa en el sistema.
 */
@Schema(description = "Respuesta de autenticación exitosa")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {

    @Schema(description = "Token de autenticación JWT generado", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Rol asignado tras la autenticación (CONSULTOR o CANDIDATO)", example = "CONSULTOR")
    private String rol;

    @Schema(description = "Identificador del usuario autenticado (ID de consultor o de candidato)", example = "1")
    private Long usuarioId;

    @Schema(description = "Identificador del cuestionario asignado (exclusivo para rol CANDIDATO)", example = "10")
    private Long cuestionarioId;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, String rol, Long usuarioId, Long cuestionarioId) {
        this.token = token;
        this.rol = rol;
        this.usuarioId = usuarioId;
        this.cuestionarioId = cuestionarioId;
    }

    public static AuthResponseDTO paraConsultor(String token, Long usuarioId) {
        return new AuthResponseDTO(token, "CONSULTOR", usuarioId, null);
    }

    public static AuthResponseDTO paraCandidato(String token, Long usuarioId, Long cuestionarioId) {
        return new AuthResponseDTO(token, "CANDIDATO", usuarioId, cuestionarioId);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getCuestionarioId() {
        return cuestionarioId;
    }

    public void setCuestionarioId(Long cuestionarioId) {
        this.cuestionarioId = cuestionarioId;
    }
}
