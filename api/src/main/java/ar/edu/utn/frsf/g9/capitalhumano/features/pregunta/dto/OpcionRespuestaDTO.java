package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * DTO para la transferencia y validación de una Opción de Respuesta (CU 021).
 */
public class OpcionRespuestaDTO {

    private Long id;

    private Integer orden;

    @NotBlank(message = "El nombre de la opción es obligatorio")
    @Size(max = 100, message = "El nombre de la opción no puede superar los 100 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción de la opción es obligatoria")
    @Size(max = 500, message = "La descripción de la opción no puede superar los 500 caracteres")
    private String descripcion;

    @NotNull(message = "La ponderación es obligatoria")
    @PositiveOrZero(message = "La ponderación debe ser mayor o igual a cero")
    private Double ponderacion;

    public OpcionRespuestaDTO() {
    }

    public OpcionRespuestaDTO(Integer orden, String nombre, String descripcion, Double ponderacion) {
        this.orden = orden;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ponderacion = ponderacion;
    }

    public OpcionRespuestaDTO(Long id, Integer orden, String nombre, String descripcion, Double ponderacion) {
        this.id = id;
        this.orden = orden;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ponderacion = ponderacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
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

    public Double getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(Double ponderacion) {
        this.ponderacion = ponderacion;
    }
}
