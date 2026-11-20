package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entidad JPA que representa una {@link Pregunta} asignada a un {@link BloqueCuestionario}.
 */
@Entity
@Table(
        name = "pregunta_asignada",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pregunta_asignada_bloque_orden", columnNames = {"bloque_cuestionario_id", "nro_orden"}),
                @UniqueConstraint(name = "uk_pregunta_asignada_bloque_pregunta", columnNames = {"bloque_cuestionario_id", "pregunta_id"})
        }
)
public class PreguntaAsignada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bloque_cuestionario_id", nullable = false)
    private BloqueCuestionario bloqueCuestionario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @Column(name = "nro_orden", nullable = false)
    private Integer nroOrden;

    @OneToOne(mappedBy = "preguntaAsignada", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RespuestaCandidato respuestaCandidato;

    public PreguntaAsignada() {
    }

    public PreguntaAsignada(Pregunta pregunta, Integer nroOrden) {
        this.pregunta = pregunta;
        this.nroOrden = nroOrden;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BloqueCuestionario getBloqueCuestionario() {
        return bloqueCuestionario;
    }

    public void setBloqueCuestionario(BloqueCuestionario bloqueCuestionario) {
        this.bloqueCuestionario = bloqueCuestionario;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }

    public Integer getNroOrden() {
        return nroOrden;
    }

    public void setNroOrden(Integer nroOrden) {
        this.nroOrden = nroOrden;
    }

    public RespuestaCandidato getRespuestaCandidato() {
        return respuestaCandidato;
    }

    public void setRespuestaCandidato(RespuestaCandidato respuestaCandidato) {
        this.respuestaCandidato = respuestaCandidato;
        if (respuestaCandidato != null && respuestaCandidato.getPreguntaAsignada() != this) {
            respuestaCandidato.setPreguntaAsignada(this);
        }
    }
}
