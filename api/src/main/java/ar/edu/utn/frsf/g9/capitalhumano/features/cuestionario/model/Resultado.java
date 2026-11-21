package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa el resultado global calculado al completar un {@link Cuestionario}
 * (CU 026 - Paso 8).
 */
@Entity
@Table(
        name = "resultado",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_resultado_cuestionario", columnNames = {"cuestionario_id"})
        }
)
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuestionario_id", nullable = false, unique = true)
    private Cuestionario cuestionario;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo;

    // TODO definir con el CU de reportes
    @Column(name = "puntaje_obtenido", precision = 10, scale = 2, nullable = false)
    private BigDecimal puntajeObtenido;

    @OneToMany(mappedBy = "resultado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoCompetencia> resultadosCompetencias = new ArrayList<>();

    public Resultado() {
    }

    public Resultado(Cuestionario cuestionario, LocalDateTime fechaCalculo, BigDecimal puntajeObtenido) {
        this.cuestionario = cuestionario;
        this.fechaCalculo = fechaCalculo;
        this.puntajeObtenido = puntajeObtenido;
    }

    public void addResultadoCompetencia(ResultadoCompetencia resultadoCompetencia) {
        if (resultadoCompetencia != null) {
            resultadosCompetencias.add(resultadoCompetencia);
            resultadoCompetencia.setResultado(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cuestionario getCuestionario() {
        return cuestionario;
    }

    public void setCuestionario(Cuestionario cuestionario) {
        this.cuestionario = cuestionario;
    }

    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }

    public void setFechaCalculo(LocalDateTime fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }

    public BigDecimal getPuntajeObtenido() {
        return puntajeObtenido;
    }

    public void setPuntajeObtenido(BigDecimal puntajeObtenido) {
        this.puntajeObtenido = puntajeObtenido;
    }

    public List<ResultadoCompetencia> getResultadosCompetencias() {
        return resultadosCompetencias;
    }

    public void setResultadosCompetencias(List<ResultadoCompetencia> resultadosCompetencias) {
        this.resultadosCompetencias = resultadosCompetencias != null ? resultadosCompetencias : new ArrayList<>();
    }
}
