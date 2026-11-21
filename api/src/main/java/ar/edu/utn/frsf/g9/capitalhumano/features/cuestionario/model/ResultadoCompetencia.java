package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa el puntaje calculado para una {@link Competencia} evaluada dentro
 * de un {@link Resultado} de cuestionario, vinculada opcionalmente a su {@link CaracteristicaPuesto}.
 */
@Entity
@Table(
        name = "resultado_competencia",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_resultado_competencia", columnNames = {"resultado_id", "competencia_id"})
        }
)
public class ResultadoCompetencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resultado_id", nullable = false)
    private Resultado resultado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caracteristica_puesto_id")
    private CaracteristicaPuesto caracteristicaPuesto;

    @Column(name = "total_competencia", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalCompetencia;

    @OneToMany(mappedBy = "resultadoCompetencia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoFactor> resultadosFactores = new ArrayList<>();

    public ResultadoCompetencia() {
    }

    public ResultadoCompetencia(Competencia competencia,
                                CaracteristicaPuesto caracteristicaPuesto,
                                BigDecimal totalCompetencia) {
        this.competencia = competencia;
        this.caracteristicaPuesto = caracteristicaPuesto;
        this.totalCompetencia = totalCompetencia;
    }

    public void addResultadoFactor(ResultadoFactor resultadoFactor) {
        if (resultadoFactor != null) {
            resultadosFactores.add(resultadoFactor);
            resultadoFactor.setResultadoCompetencia(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resultado getResultado() {
        return resultado;
    }

    public void setResultado(Resultado resultado) {
        this.resultado = resultado;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    public void setCompetencia(Competencia competencia) {
        this.competencia = competencia;
    }

    public CaracteristicaPuesto getCaracteristicaPuesto() {
        return caracteristicaPuesto;
    }

    public void setCaracteristicaPuesto(CaracteristicaPuesto caracteristicaPuesto) {
        this.caracteristicaPuesto = caracteristicaPuesto;
    }

    public BigDecimal getTotalCompetencia() {
        return totalCompetencia;
    }

    public void setTotalCompetencia(BigDecimal totalCompetencia) {
        this.totalCompetencia = totalCompetencia;
    }

    public List<ResultadoFactor> getResultadosFactores() {
        return resultadosFactores;
    }

    public void setResultadosFactores(List<ResultadoFactor> resultadosFactores) {
        this.resultadosFactores = resultadosFactores != null ? resultadosFactores : new ArrayList<>();
    }
}
