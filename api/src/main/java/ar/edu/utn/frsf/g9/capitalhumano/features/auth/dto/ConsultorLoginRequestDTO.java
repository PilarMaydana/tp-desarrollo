package ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de autenticación de un usuario con rol Consultor mediante LDAP.
 */
@Schema(description = "Solicitud de inicio de sesión para consultores")
public class ConsultorLoginRequestDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 15, message = "El identificador de usuario no puede superar los 15 caracteres")
    @Schema(description = "Identificador de usuario (máx 15 caracteres, insensible a mayúsculas)", example = "ADMIN")
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 20, message = "La contraseña no puede superar los 20 caracteres")
    @Schema(description = "Contraseña del usuario en LDAP (máx 20 caracteres)", example = "admin123")
    private String contrasena;

    public ConsultorLoginRequestDTO() {
    }

    public ConsultorLoginRequestDTO(String usuario, String contrasena) {
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
