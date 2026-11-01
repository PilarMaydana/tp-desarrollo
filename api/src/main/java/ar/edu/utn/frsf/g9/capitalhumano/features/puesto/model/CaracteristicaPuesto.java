package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad JPA que representa la característica de un puesto (ponderación requerida para una competencia).
 */
@Entity
@Table(name = "caracteristica_puesto")
public class CaracteristicaPuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puesto_id", nullable = false)
    private Puesto puesto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @Column(name = "ponderacion_requerida", nullable = false)
    private Double ponderacionRequerida;

    public CaracteristicaPuesto() {
    }

    public CaracteristicaPuesto(Long id, Puesto puesto, Competencia competencia, Double ponderacionRequerida) {
        this.id = id;
        this.puesto = puesto;
        this.competencia = competencia;
        this.ponderacionRequerida = ponderacionRequerida;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Puesto getPuesto() {
        return puesto;
    }

    public void setPuesto(Puesto puesto) {
        this.puesto = puesto;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    public void setCompetencia(Competencia competencia) {
        this.competencia = competencia;
    }

    public Double getPonderacionRequerida() {
        return ponderacionRequerida;
    }

    public void setPonderacionRequerida(Double ponderacionRequerida) {
        this.ponderacionRequerida = ponderacionRequerida;
    }
}

