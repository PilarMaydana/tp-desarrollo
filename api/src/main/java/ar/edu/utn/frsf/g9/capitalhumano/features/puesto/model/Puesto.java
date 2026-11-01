package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model;

import ar.edu.utn.frsf.g9.capitalhumano.shared.model.EntidadAuditable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa un Puesto o Función en el sistema (CU 009).
 */
@Entity
@Table(name = "puesto")
public class Puesto extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", length = 50, nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", length = 100, nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion", length = 500, nullable = false)
    private String descripcion;

    @Column(name = "empresa", length = 100, nullable = false)
    private String empresa;

    @OneToMany(mappedBy = "puesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaracteristicaPuesto> caracteristicas = new ArrayList<>();

    public Puesto() {
    }

    public Puesto(Long id, String codigo, String nombre, String descripcion, String empresa) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.empresa = empresa;
    }

    public void addCaracteristica(CaracteristicaPuesto caracteristica) {
        caracteristicas.add(caracteristica);
        caracteristica.setPuesto(this);
    }

    public void removeCaracteristica(CaracteristicaPuesto caracteristica) {
        caracteristicas.remove(caracteristica);
        caracteristica.setPuesto(null);
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

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public List<CaracteristicaPuesto> getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(List<CaracteristicaPuesto> caracteristicas) {
        this.caracteristicas = caracteristicas != null ? caracteristicas : new ArrayList<>();
    }
}

