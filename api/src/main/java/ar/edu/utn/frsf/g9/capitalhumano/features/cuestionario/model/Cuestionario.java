package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.shared.model.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un Cuestionario asignado a un candidato para un proceso de evaluación.
 */
@Entity
@Table(name = "cuestionario")
public class Cuestionario extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidato_id", nullable = false)
    private Candidato candidato;

    @Column(name = "puesto_id")
    private Long puestoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoCuestionario estado;

    @Column(name = "clave_acceso", length = 8, nullable = false)
    private String claveAcceso;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @Column(name = "tiempo_restante_segundos")
    private Integer tiempoRestanteSegundos;

    @Column(name = "ultimo_bloque_respondido")
    private Integer ultimoBloqueRespondido;

    public Cuestionario() {
    }

    public Cuestionario(Long id, Candidato candidato, EstadoCuestionario estado, String claveAcceso) {
        this.id = id;
        this.candidato = candidato;
        this.estado = estado;
        this.claveAcceso = claveAcceso;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Candidato getCandidato() {
        return candidato;
    }

    public void setCandidato(Candidato candidato) {
        this.candidato = candidato;
    }

    public Long getPuestoId() {
        return puestoId;
    }

    public void setPuestoId(Long puestoId) {
        this.puestoId = puestoId;
    }

    public EstadoCuestionario getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuestionario estado) {
        this.estado = estado;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public Integer getTiempoRestanteSegundos() {
        return tiempoRestanteSegundos;
    }

    public void setTiempoRestanteSegundos(Integer tiempoRestanteSegundos) {
        this.tiempoRestanteSegundos = tiempoRestanteSegundos;
    }

    public Integer getUltimoBloqueRespondido() {
        return ultimoBloqueRespondido;
    }

    public void setUltimoBloqueRespondido(Integer ultimoBloqueRespondido) {
        this.ultimoBloqueRespondido = ultimoBloqueRespondido;
    }
}
