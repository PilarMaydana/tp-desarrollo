package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.model;

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
 * Entidad JPA hija que representa un ítem individual dentro de una {@link OpcionRespuesta} (CU 029).
 */
@Entity
@Table(
        name = "item_opcion_respuesta",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_item_opcion_orden", columnNames = {"opcion_respuesta_id", "orden"}),
                @UniqueConstraint(name = "uk_item_opcion_descripcion", columnNames = {"opcion_respuesta_id", "descripcion"})
        }
)
public class ItemOpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opcion_respuesta_id", nullable = false)
    private OpcionRespuesta opcionRespuesta;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "descripcion", length = 500, nullable = false)
    private String descripcion;

    public ItemOpcionRespuesta() {
    }

    public ItemOpcionRespuesta(Long id, OpcionRespuesta opcionRespuesta, Integer orden, String descripcion) {
        this.id = id;
        this.opcionRespuesta = opcionRespuesta;
        this.orden = orden;
        this.descripcion = descripcion;
    }

    public ItemOpcionRespuesta(Integer orden, String descripcion) {
        this.orden = orden;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OpcionRespuesta getOpcionRespuesta() {
        return opcionRespuesta;
    }

    public void setOpcionRespuesta(OpcionRespuesta opcionRespuesta) {
        this.opcionRespuesta = opcionRespuesta;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

