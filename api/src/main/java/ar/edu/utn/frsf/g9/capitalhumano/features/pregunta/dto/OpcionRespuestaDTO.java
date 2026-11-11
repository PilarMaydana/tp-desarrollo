package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para la transferencia y validación de una Opción de Respuesta (CU 020 / CU 021).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpcionRespuestaDTO {

    private Long id;

    @NotBlank(message = "El nombre de la opción es obligatorio")
    @Size(max = 100, message = "El nombre de la opción no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción de la opción no puede superar los 500 caracteres")
    private String descripcion;

    @JsonAlias({"nro_orden", "numeroOrden", "orden"})
    private Integer nroOrden;

    @NotNull(message = "La ponderación es obligatoria")
    @DecimalMin(value = "0.0", message = "La ponderación debe ser mayor o igual a 0")
    private Double ponderacion;

    public OpcionRespuestaDTO() {
    }

    public OpcionRespuestaDTO(String nombre, String descripcion, Double ponderacion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ponderacion = ponderacion;
    }

    public OpcionRespuestaDTO(String nombre, String descripcion, Integer nroOrden, Double ponderacion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nroOrden = nroOrden;
        this.ponderacion = ponderacion;
    }

    public OpcionRespuestaDTO(Long id, String nombre, String descripcion, Double ponderacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ponderacion = ponderacion;
    }

    public OpcionRespuestaDTO(Long id, String nombre, String descripcion, Integer nroOrden, Double ponderacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nroOrden = nroOrden;
        this.ponderacion = ponderacion;
    }

    public OpcionRespuestaDTO(Integer orden, String nombre, String descripcion, Double ponderacion) {
        this.nroOrden = orden;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ponderacion = ponderacion;
    }

    public OpcionRespuestaDTO(Long id, Integer orden, String nombre, String descripcion, Double ponderacion) {
        this.id = id;
        this.nroOrden = orden;
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

    public Integer getNroOrden() {
        return nroOrden;
    }

    public void setNroOrden(Integer nroOrden) {
        this.nroOrden = nroOrden;
    }

    public Integer getOrden() {
        return nroOrden;
    }

    public void setOrden(Integer orden) {
        this.nroOrden = orden;
    }

    public Double getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(Double ponderacion) {
        this.ponderacion = ponderacion;
    }
}
