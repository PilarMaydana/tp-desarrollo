package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para la transferencia de datos de una característica de puesto.
 */
public class CaracteristicaDTO {

    private Long id;

    @NotNull(message = "El ID de la competencia es obligatorio")
    private Long competenciaId;

    private String nombreCompetencia;

    @NotNull(message = "La ponderación es obligatoria")
    @DecimalMin(value = "0.0", message = "La ponderación mínima es 0")
    @DecimalMax(value = "10.0", message = "La ponderación máxima es 10")
    private Double ponderacion;

    public CaracteristicaDTO() {
    }

    public CaracteristicaDTO(Long id, Long competenciaId, String nombreCompetencia, Double ponderacion) {
        this.id = id;
        this.competenciaId = competenciaId;
        this.nombreCompetencia = nombreCompetencia;
        this.ponderacion = ponderacion;
    }

    public CaracteristicaDTO(Long competenciaId, Double ponderacion) {
        this(null, competenciaId, null, ponderacion);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompetenciaId() {
        return competenciaId;
    }

    public void setCompetenciaId(Long competenciaId) {
        this.competenciaId = competenciaId;
    }

    public String getNombreCompetencia() {
        return nombreCompetencia;
    }

    public void setNombreCompetencia(String nombreCompetencia) {
        this.nombreCompetencia = nombreCompetencia;
    }

    public Double getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(Double ponderacion) {
        this.ponderacion = ponderacion;
    }

    public Double getPonderacionRequerida() {
        return ponderacion;
    }

    @JsonProperty("ponderacionRequerida")
    public void setPonderacionRequerida(Double ponderacionRequerida) {
        if (this.ponderacion == null) {
            this.ponderacion = ponderacionRequerida;
        }
    }
}

