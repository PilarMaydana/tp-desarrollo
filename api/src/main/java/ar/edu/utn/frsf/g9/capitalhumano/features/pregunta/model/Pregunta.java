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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una Pregunta evaluable en el sistema (CU 020 / CU 021).
 */
@Entity
@Table(name = "pregunta")
public class Pregunta extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;

    @Column(name = "enunciado", length = 1000, nullable = false)
    private String enunciado;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factor_id", nullable = false)
    private Factor factor;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nroOrden ASC")
    private List<OpcionRespuesta> opciones = new ArrayList<>();

    public Pregunta() {
        this.setEliminado(false);
    }

    public Pregunta(Long id, String nombre, String enunciado, String descripcion, Factor factor) {
        this.id = id;
        this.nombre = nombre;
        this.enunciado = enunciado;
        this.descripcion = descripcion;
        this.factor = factor;
        this.setEliminado(false);
    }

    public void addOpcion(OpcionRespuesta opcion) {
        opciones.add(opcion);
        opcion.setPregunta(this);
    }

    public void removeOpcion(OpcionRespuesta opcion) {
        opciones.remove(opcion);
        opcion.setPregunta(null);
    }

    public void clearOpciones() {
        for (OpcionRespuesta opcion : new ArrayList<>(opciones)) {
            removeOpcion(opcion);
        }
    }

    // Aliases para compatibilidad con CU 021
    public void addOpcionRespuesta(OpcionRespuesta opcion) {
        addOpcion(opcion);
    }

    public void removeOpcionRespuesta(OpcionRespuesta opcion) {
        removeOpcion(opcion);
    }

    public List<OpcionRespuesta> getOpcionesRespuesta() {
        return getOpciones();
    }

    public void setOpcionesRespuesta(List<OpcionRespuesta> opcionesRespuesta) {
        setOpciones(opcionesRespuesta);
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

    public List<OpcionRespuesta> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionRespuesta> opciones) {
        this.opciones = opciones != null ? opciones : new ArrayList<>();
    }
}
