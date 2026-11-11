package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para el alta y representación de una Opción de Respuesta (CU 029).
 */
@Schema(name = "OpcionRespuestaConjuntoDTO", description = "Datos de una opción de respuesta y su lista de ítems (CU 029)")
public class OpcionRespuestaDTO {

    @Schema(description = "Identificador único de la opción de respuesta", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Schema(description = "Nombre único de la opción de respuesta", example = "CALIFICACIÓN")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    @Schema(description = "Descripción de la opción de respuesta", example = "ESCALA DE CALIFICACIÓN DEL DESEMPEÑO")
    private String descripcion;

    @NotNull(message = "La lista de ítems es obligatoria")
    @Size(min = 2, message = "La opción de respuesta debe tener al menos 2 ítems")
    @Valid
    @Schema(description = "Lista de ítems que componen la opción de respuesta (mínimo 2)")
    private List<ItemOpcionRespuestaDTO> items;

    public OpcionRespuestaDTO() {
    }

    public OpcionRespuestaDTO(Long id, String nombre, String descripcion, List<ItemOpcionRespuestaDTO> items) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<ItemOpcionRespuestaDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemOpcionRespuestaDTO> items) {
        this.items = items;
    }
}

