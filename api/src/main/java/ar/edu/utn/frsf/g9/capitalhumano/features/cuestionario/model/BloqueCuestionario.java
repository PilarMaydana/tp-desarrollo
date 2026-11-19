package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

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
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa un bloque (paso del wizard) dentro de un {@link Cuestionario}.
 */
@Entity
@Table(
        name = "bloque_cuestionario",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_bloque_cuestionario_nro", columnNames = {"cuestionario_id", "nro_bloque"})
        }
)
public class BloqueCuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuestionario_id", nullable = false)
    private Cuestionario cuestionario;

    @Column(name = "nro_bloque", nullable = false)
    private Integer nroBloque;

    @Column(name = "respondido", nullable = false)
    private Boolean respondido = Boolean.FALSE;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @OneToMany(mappedBy = "bloqueCuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nroOrden ASC")
    private List<PreguntaAsignada> preguntasAsignadas = new ArrayList<>();

    public BloqueCuestionario() {
    }

    public BloqueCuestionario(Integer nroBloque) {
        this.nroBloque = nroBloque;
        this.respondido = Boolean.FALSE;
    }

    public void addPreguntaAsignada(PreguntaAsignada preguntaAsignada) {
        if (preguntaAsignada != null) {
            preguntasAsignadas.add(preguntaAsignada);
            preguntaAsignada.setBloqueCuestionario(this);
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

    public Integer getNroBloque() {
        return nroBloque;
    }

    public void setNroBloque(Integer nroBloque) {
        this.nroBloque = nroBloque;
    }

    public Boolean getRespondido() {
        return respondido;
    }

    public void setRespondido(Boolean respondido) {
        this.respondido = respondido != null ? respondido : Boolean.FALSE;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public List<PreguntaAsignada> getPreguntasAsignadas() {
        return preguntasAsignadas;
    }

    public void setPreguntasAsignadas(List<PreguntaAsignada> preguntasAsignadas) {
        this.preguntasAsignadas = preguntasAsignadas != null ? preguntasAsignadas : new ArrayList<>();
    }
}
