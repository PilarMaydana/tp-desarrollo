package ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de autenticación de un Candidato mediante credenciales de Cuestionario.
 */
@Schema(description = "Solicitud de inicio de sesión para candidatos a responder cuestionario")
public class CandidatoLoginRequestDTO {

    @NotNull(message = "El tipo de documento es obligatorio")
    @Schema(description = "Tipo de documento de identidad (DNI, LE, LC, PP)", example = "DNI")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Schema(description = "Número de documento del candidato", example = "12345678")
    private String numeroDocumento;

    @NotBlank(message = "La clave es obligatoria")
    @Size(max = 8, message = "La clave no puede superar los 8 caracteres")
    @Schema(description = "Clave de acceso al cuestionario (máx 8 caracteres)", example = "CLAVE123")
    private String clave;

    public CandidatoLoginRequestDTO() {
    }

    public CandidatoLoginRequestDTO(TipoDocumento tipoDocumento, String numeroDocumento, String clave) {
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.clave = clave;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}
