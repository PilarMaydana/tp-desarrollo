package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para la creación y representación de una Pregunta con sus opciones de respuesta (CU 021).
 */
public class PreguntaDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotBlank(message = "El enunciado es obligatorio")
    @Size(max = 500, message = "El enunciado no puede superar los 500 caracteres")
    private String enunciado;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @NotBlank(message = "El código de factor es obligatorio")
    private String codigoFactor;

    private String codigoCompetencia;

    @NotNull(message = "Las opciones de respuesta son obligatorias")
    @Size(min = 2, message = "Debe ingresar al menos 2 opciones de respuesta")
    @Valid
    private List<OpcionRespuestaDTO> opcionesRespuesta = new ArrayList<>();

    public PreguntaDTO() {
    }

    public PreguntaDTO(Long id, String nombre, String enunciado, String descripcion, String codigoFactor, List<OpcionRespuestaDTO> opcionesRespuesta) {
        this.id = id;
        this.nombre = nombre;
        this.enunciado = enunciado;
        this.descripcion = descripcion;
        this.codigoFactor = codigoFactor;
        this.opcionesRespuesta = opcionesRespuesta != null ? opcionesRespuesta : new ArrayList<>();
    }

    public PreguntaDTO(Long id, String nombre, String enunciado, String descripcion, String codigoFactor, String codigoCompetencia, List<OpcionRespuestaDTO> opcionesRespuesta) {
        this.id = id;
        this.nombre = nombre;
        this.enunciado = enunciado;
        this.descripcion = descripcion;
        this.codigoFactor = codigoFactor;
        this.codigoCompetencia = codigoCompetencia;
        this.opcionesRespuesta = opcionesRespuesta != null ? opcionesRespuesta : new ArrayList<>();
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

    public String getCodigoFactor() {
        return codigoFactor;
    }

    public void setCodigoFactor(String codigoFactor) {
        this.codigoFactor = codigoFactor;
    }

    public String getCodigoCompetencia() {
        return codigoCompetencia;
    }

    public void setCodigoCompetencia(String codigoCompetencia) {
        this.codigoCompetencia = codigoCompetencia;
    }

    public List<OpcionRespuestaDTO> getOpcionesRespuesta() {
        return opcionesRespuesta;
    }

    public void setOpcionesRespuesta(List<OpcionRespuestaDTO> opcionesRespuesta) {
        this.opcionesRespuesta = opcionesRespuesta != null ? opcionesRespuesta : new ArrayList<>();
    }
}

