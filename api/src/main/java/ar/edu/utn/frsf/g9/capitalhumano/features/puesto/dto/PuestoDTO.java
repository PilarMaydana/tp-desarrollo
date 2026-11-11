package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para la transferencia de datos en el alta y consulta de Puestos (CU 009).
 */
public class PuestoDTO {

    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50, message = "El código no puede superar los 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @NotBlank(message = "La empresa es obligatoria")
    @Size(max = 100, message = "La empresa no puede superar los 100 caracteres")
    private String empresa;

    @Valid
    @NotEmpty(message = "Debe ingresar al menos una característica")
    private List<CaracteristicaDTO> caracteristicas = new ArrayList<>();

    public PuestoDTO() {
    }

    public PuestoDTO(Long id, String codigo, String nombre, String descripcion, String empresa, List<CaracteristicaDTO> caracteristicas) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.empresa = empresa;
        this.caracteristicas = caracteristicas != null ? caracteristicas : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombrePuesto() {
        return nombre;
    }

    @JsonProperty("nombrePuesto")
    public void setNombrePuesto(String nombrePuesto) {
        if (this.nombre == null || this.nombre.isBlank()) {
            this.nombre = nombrePuesto;
        }
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getEmpresaNombre() {
        return empresa;
    }

    @JsonProperty("empresaNombre")
    public void setEmpresaNombre(String empresaNombre) {
        if (this.empresa == null || this.empresa.isBlank()) {
            this.empresa = empresaNombre;
        }
    }

    public List<CaracteristicaDTO> getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(List<CaracteristicaDTO> caracteristicas) {
        this.caracteristicas = caracteristicas != null ? caracteristicas : new ArrayList<>();
    }

    public List<CaracteristicaDTO> getCompetencias() {
        return caracteristicas;
    }

    @JsonProperty("competencias")
    public void setCompetencias(List<CaracteristicaDTO> competencias) {
        if (this.caracteristicas == null || this.caracteristicas.isEmpty()) {
            this.caracteristicas = competencias != null ? competencias : new ArrayList<>();
        }
    }
}

