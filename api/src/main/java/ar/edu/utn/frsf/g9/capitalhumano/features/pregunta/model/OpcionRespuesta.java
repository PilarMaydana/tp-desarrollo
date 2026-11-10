package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad JPA que representa una Opción de Respuesta vinculada a una Pregunta (CU 020).
 */
@Entity
@Table(name = "opcion_respuesta")
public class OpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "nro_orden", nullable = false)
    private Integer nroOrden;

    @Column(name = "ponderacion", nullable = false)
    private Double ponderacion;

    public OpcionRespuesta() {
    }

    public OpcionRespuesta(Long id, Pregunta pregunta, String nombre, String descripcion, Integer nroOrden, Double ponderacion) {
        this.id = id;
        this.pregunta = pregunta;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nroOrden = nroOrden;
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

    public Integer getNroOrden() {
        return nroOrden;
    }

    public void setNroOrden(Integer nroOrden) {
        this.nroOrden = nroOrden;
    }

    public Double getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(Double ponderacion) {
        this.ponderacion = ponderacion;
    }
}
