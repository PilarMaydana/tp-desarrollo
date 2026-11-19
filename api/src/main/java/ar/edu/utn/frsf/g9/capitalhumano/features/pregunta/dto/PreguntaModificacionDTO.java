package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO de entrada para la modificación de una Pregunta existente (CU 022).
 *
 * <p>No incluye campos de Competencia ni Factor ya que ambos son de solo lectura
 * durante la modificación.</p>
 */
@Schema(description = "Datos modificables de una pregunta y sus opciones de respuesta ponderadas (CU 022)")
public class PreguntaModificacionDTO {

    @Schema(description = "Nombre identificatorio de la pregunta", example = "PREGUNTA POO", maxLength = 255)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre de la pregunta no puede superar los 255 caracteres")
    private String nombre;

    @Schema(description = "Texto o enunciado de la pregunta", example = "¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS?", maxLength = 1000)
    @NotBlank(message = "El enunciado es obligatorio")
    @Size(max = 1000, message = "El enunciado de la pregunta no puede superar los 1000 caracteres")
    private String enunciado;

    @Schema(description = "Descripción opcional de la pregunta", example = "CONCEPTOS BÁSICOS DE PARADIGMA", maxLength = 500)
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @Schema(description = "Listado completo de opciones de respuesta ponderadas (mínimo 2; las ponderaciones deben sumar 10)")
    @NotNull(message = "Las opciones de respuesta son obligatorias")
    @Size(min = 2, message = "Debe ingresar al menos 2 opciones de respuesta")
    @JsonAlias({"opciones", "opcionesRespuesta", "opciones_respuesta"})
    private List<@NotNull(message = "Cada opción de respuesta es obligatoria") @Valid OpcionRespuestaDTO> opciones;

    public PreguntaModificacionDTO() {
    }

    public PreguntaModificacionDTO(String nombre,
                                   String enunciado,
                                   String descripcion,
                                   List<OpcionRespuestaDTO> opciones) {
        this.nombre = nombre;
        this.enunciado = enunciado;
        this.descripcion = descripcion;
        this.opciones = opciones;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<OpcionRespuestaDTO> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionRespuestaDTO> opciones) {
        this.opciones = opciones;
    }
}
