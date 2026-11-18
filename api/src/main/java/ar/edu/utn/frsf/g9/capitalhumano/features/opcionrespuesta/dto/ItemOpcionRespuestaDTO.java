package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO que representa un ítem dentro de una Opción de Respuesta (CU 029).
 */
@Schema(name = "ItemOpcionRespuestaDTO", description = "Ítem individual de una opción de respuesta con orden de visualización y descripción")
public class ItemOpcionRespuestaDTO {

    @NotNull(message = "El orden de visualización es obligatorio")
    @Schema(description = "Orden de visualización del ítem (consecutivo a partir de 1)", example = "1")
    private Integer orden;

    @NotBlank(message = "La descripción del ítem es obligatoria")
    @Size(max = 500, message = "La descripción del ítem no puede superar los 500 caracteres")
    @Schema(description = "Descripción literal del ítem", example = "NUNCA")
    private String descripcion;

    public ItemOpcionRespuestaDTO() {
    }

    public ItemOpcionRespuestaDTO(Integer orden, String descripcion) {
        this.orden = orden;
        this.descripcion = descripcion;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

