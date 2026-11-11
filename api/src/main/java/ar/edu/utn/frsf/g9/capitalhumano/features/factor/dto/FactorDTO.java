package ar.edu.utn.frsf.g9.capitalhumano.features.factor.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para la transferencia de datos en el alta y representación de Factores (CU 017 / CU 021).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FactorDTO {

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

    @NotNull(message = "El ID de competencia es obligatorio")
    @JsonAlias({"competencia_id"})
    private Long competenciaId;

    private String competenciaNombre;

    private String codigoCompetencia;

    @NotNull(message = "El número de orden es obligatorio")
    @Min(value = 1, message = "El número de orden debe ser mayor a 0")
    @JsonAlias({"nro_orden", "numeroOrden"})
    private Integer nroOrden;

    public FactorDTO() {
    }

    public FactorDTO(Long id, String codigo, String nombre, String descripcion, Long competenciaId, Integer nroOrden) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.competenciaId = competenciaId;
        this.nroOrden = nroOrden;
    }

    public FactorDTO(Long id, String codigo, String nombre, String descripcion, Long competenciaId, String competenciaNombre, Integer nroOrden) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.competenciaId = competenciaId;
        this.competenciaNombre = competenciaNombre;
        this.nroOrden = nroOrden;
    }

    public FactorDTO(Long id, String codigo, String nombre, String descripcion, Long competenciaId, String competenciaNombre, String codigoCompetencia, Integer nroOrden) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.competenciaId = competenciaId;
        this.competenciaNombre = competenciaNombre;
        this.codigoCompetencia = codigoCompetencia;
        this.nroOrden = nroOrden;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getCompetenciaId() {
        return competenciaId;
    }

    public void setCompetenciaId(Long competenciaId) {
        this.competenciaId = competenciaId;
    }

    public String getCompetenciaNombre() {
        return competenciaNombre;
    }

    public void setCompetenciaNombre(String competenciaNombre) {
        this.competenciaNombre = competenciaNombre;
    }

    public String getCodigoCompetencia() {
        return codigoCompetencia;
    }

    public void setCodigoCompetencia(String codigoCompetencia) {
        this.codigoCompetencia = codigoCompetencia;
    }

    public Integer getNroOrden() {
        return nroOrden;
    }

    public void setNroOrden(Integer nroOrden) {
        this.nroOrden = nroOrden;
    }
}
