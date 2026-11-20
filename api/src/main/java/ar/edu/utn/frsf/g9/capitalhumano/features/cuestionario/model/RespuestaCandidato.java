package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
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

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la respuesta seleccionada por un candidato para una
 * {@link PreguntaAsignada} específica dentro de un bloque de cuestionario (CU 026).
 */
@Entity
@Table(
        name = "respuesta_candidato",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_respuesta_candidato_pregunta_asignada", columnNames = {"pregunta_asignada_id"})
        }
)
public class RespuestaCandidato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_asignada_id", nullable = false, unique = true)
    private PreguntaAsignada preguntaAsignada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opcion_respuesta_id", nullable = false)
    private OpcionRespuesta opcionElegida;

    @Column(name = "fecha_respuesta", nullable = false)
    private LocalDateTime fechaRespuesta;

    public RespuestaCandidato() {
    }

    public RespuestaCandidato(PreguntaAsignada preguntaAsignada,
                              OpcionRespuesta opcionElegida,
                              LocalDateTime fechaRespuesta) {
        this.preguntaAsignada = preguntaAsignada;
        this.opcionElegida = opcionElegida;
        this.fechaRespuesta = fechaRespuesta;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PreguntaAsignada getPreguntaAsignada() {
        return preguntaAsignada;
    }

    public void setPreguntaAsignada(PreguntaAsignada preguntaAsignada) {
        this.preguntaAsignada = preguntaAsignada;
    }

    public OpcionRespuesta getOpcionElegida() {
        return opcionElegida;
    }

    public void setOpcionElegida(OpcionRespuesta opcionElegida) {
        this.opcionElegida = opcionElegida;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
}
