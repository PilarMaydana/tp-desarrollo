package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una Opción de Respuesta como conjunto reutilizable de ítems (CU 029).
 */
@Entity
@Table(name = "opcion_respuesta")
public class OpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", length = 100, nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion", length = 500, nullable = false)
    private String descripcion;

    @OneToMany(mappedBy = "opcionRespuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<ItemOpcionRespuesta> items = new ArrayList<>();

    public OpcionRespuesta() {
    }

    public OpcionRespuesta(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public void addItem(ItemOpcionRespuesta item) {
        items.add(item);
        item.setOpcionRespuesta(this);
    }

    public void removeItem(ItemOpcionRespuesta item) {
        items.remove(item);
        item.setOpcionRespuesta(null);
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<ItemOpcionRespuesta> getItems() {
        return items;
    }

    public void setItems(List<ItemOpcionRespuesta> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}

