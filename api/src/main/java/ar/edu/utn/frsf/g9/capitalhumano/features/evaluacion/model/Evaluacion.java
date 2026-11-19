package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una Evaluación de candidatos para un Puesto específico (CU 025).
 */
@Entity
@Table(name = "evaluacion")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puesto_id", nullable = false)
    private Puesto puesto;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "consultor_id", nullable = false)
    private Long consultorId;

    @Column(name = "tiempo_maximo_minutos", nullable = false)
    private Integer tiempoMaximoMinutos;

    @Column(name = "cantidad_preguntas_por_bloque", nullable = false)
    private Integer cantidadPreguntasPorBloque;

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Cuestionario> cuestionarios = new ArrayList<>();

    public Evaluacion() {
    }

    public Evaluacion(Puesto puesto,
                      LocalDateTime fechaCreacion,
                      Long consultorId,
                      Integer tiempoMaximoMinutos,
                      Integer cantidadPreguntasPorBloque) {
        this.puesto = puesto;
        this.fechaCreacion = fechaCreacion;
        this.consultorId = consultorId;
        this.tiempoMaximoMinutos = tiempoMaximoMinutos;
        this.cantidadPreguntasPorBloque = cantidadPreguntasPorBloque;
    }

    public void addCuestionario(Cuestionario cuestionario) {
        if (cuestionario != null) {
            cuestionarios.add(cuestionario);
            cuestionario.setEvaluacion(this);
            cuestionario.setPuesto(this.puesto);
        }
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getConsultorId() {
        return consultorId;
    }

    public void setConsultorId(Long consultorId) {
        this.consultorId = consultorId;
    }

    public Integer getTiempoMaximoMinutos() {
        return tiempoMaximoMinutos;
    }

    public void setTiempoMaximoMinutos(Integer tiempoMaximoMinutos) {
        this.tiempoMaximoMinutos = tiempoMaximoMinutos;
    }

    public Integer getCantidadPreguntasPorBloque() {
        return cantidadPreguntasPorBloque;
    }

    public void setCantidadPreguntasPorBloque(Integer cantidadPreguntasPorBloque) {
        this.cantidadPreguntasPorBloque = cantidadPreguntasPorBloque;
    }

    public List<Cuestionario> getCuestionarios() {
        return cuestionarios;
    }

    public void setCuestionarios(List<Cuestionario> cuestionarios) {
        this.cuestionarios = cuestionarios != null ? cuestionarios : new ArrayList<>();
    }
}
