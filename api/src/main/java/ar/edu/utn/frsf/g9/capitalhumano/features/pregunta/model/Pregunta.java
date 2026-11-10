package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.shared.model.EntidadAuditable;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una Pregunta en el banco de evaluación (CU 021).
 */
@Entity
@Table(name = "pregunta", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pregunta_factor_nombre", columnNames = {"factor_id", "nombre"}),
        @UniqueConstraint(name = "uk_pregunta_factor_enunciado", columnNames = {"factor_id", "enunciado"})
})
public class Pregunta extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "enunciado", length = 500, nullable = false)
    private String enunciado;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factor_id", nullable = false)
    private Factor factor;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpcionRespuesta> opcionesRespuesta = new ArrayList<>();

    public Pregunta() {
    }

    public Pregunta(Long id, String nombre, String enunciado, String descripcion, Factor factor) {
        this.id = id;
        this.nombre = nombre;
        this.enunciado = enunciado;
        this.descripcion = descripcion;
        this.factor = factor;
    }

    public void addOpcionRespuesta(OpcionRespuesta opcion) {
        opcionesRespuesta.add(opcion);
        opcion.setPregunta(this);
    }

    public void removeOpcionRespuesta(OpcionRespuesta opcion) {
        opcionesRespuesta.remove(opcion);
        opcion.setPregunta(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Factor getFactor() {
        return factor;
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    public List<OpcionRespuesta> getOpcionesRespuesta() {
        return opcionesRespuesta;
    }

    public void setOpcionesRespuesta(List<OpcionRespuesta> opcionesRespuesta) {
        this.opcionesRespuesta = opcionesRespuesta != null ? opcionesRespuesta : new ArrayList<>();
    }
}
