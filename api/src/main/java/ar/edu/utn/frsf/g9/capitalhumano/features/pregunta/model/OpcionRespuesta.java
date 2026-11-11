package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model;

import ar.edu.utn.frsf.g9.capitalhumano.shared.model.EntidadAuditable;
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

/**
 * Entidad JPA que representa una Opción de Respuesta asociada a una Pregunta (CU 021 / CU 029).
 */
@Entity
@Table(name = "opcion_respuesta", uniqueConstraints = {
        @UniqueConstraint(name = "uk_opcion_pregunta_orden", columnNames = {"pregunta_id", "orden"})
})
public class OpcionRespuesta extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 500, nullable = false)
    private String descripcion;

    @Column(name = "ponderacion", nullable = false)
    private Double ponderacion;

    public OpcionRespuesta() {
    }

    public OpcionRespuesta(Long id, Integer orden, String nombre, String descripcion, Double ponderacion) {
        this.id = id;
        this.orden = orden;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ponderacion = ponderacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(Double ponderacion) {
        this.ponderacion = ponderacion;
    }
}

