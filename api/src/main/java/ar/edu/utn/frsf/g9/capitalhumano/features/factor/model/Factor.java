package ar.edu.utn.frsf.g9.capitalhumano.features.factor.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
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
 * Entidad JPA que representa un Factor evaluable asociado a una Competencia (CU 017).
 */
@Entity
@Table(name = "factor", uniqueConstraints = {
        @UniqueConstraint(name = "uk_factor_codigo", columnNames = {"codigo"}),
        @UniqueConstraint(name = "uk_factor_nombre", columnNames = {"nombre"}),
        @UniqueConstraint(name = "uk_factor_competencia_orden", columnNames = {"competencia_id", "nro_orden"})
})
public class Factor extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", length = 50, nullable = false)
    private String codigo;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 500, nullable = false)
    private String descripcion;

    @Column(name = "nro_orden", nullable = false)
    private Integer nroOrden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    public Factor() {
        this.setEliminado(false);
    }

    public Factor(Long id, String codigo, String nombre, String descripcion, Integer nroOrden, Competencia competencia) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nroOrden = nroOrden;
        this.competencia = competencia;
        this.setEliminado(false);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public Competencia getCompetencia() {
        return competencia;
    }

    public void setCompetencia(Competencia competencia) {
        this.competencia = competencia;
    }
}
