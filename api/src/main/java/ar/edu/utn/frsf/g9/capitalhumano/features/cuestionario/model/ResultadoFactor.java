package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

/**
 * Entidad JPA que representa el puntaje calculado para un {@link Factor} evaluado dentro
 * de un {@link ResultadoCompetencia} (CU 026 - Paso 8).
 */
@Entity
@Table(
        name = "resultado_factor",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_resultado_factor", columnNames = {"resultado_competencia_id", "factor_id"})
        }
)
public class ResultadoFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resultado_competencia_id", nullable = false)
    private ResultadoCompetencia resultadoCompetencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factor_id", nullable = false)
    private Factor factor;

    @Column(name = "total_factor", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalFactor;

    public ResultadoFactor() {
    }

    public ResultadoFactor(Factor factor, BigDecimal totalFactor) {
        this.factor = factor;
        this.totalFactor = totalFactor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResultadoCompetencia getResultadoCompetencia() {
        return resultadoCompetencia;
    }

    public void setResultadoCompetencia(ResultadoCompetencia resultadoCompetencia) {
        this.resultadoCompetencia = resultadoCompetencia;
    }

    public Factor getFactor() {
        return factor;
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    public BigDecimal getTotalFactor() {
        return totalFactor;
    }

    public void setTotalFactor(BigDecimal totalFactor) {
        this.totalFactor = totalFactor;
    }
}
