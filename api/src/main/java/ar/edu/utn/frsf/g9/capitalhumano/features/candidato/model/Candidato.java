package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model;

import ar.edu.utn.frsf.g9.capitalhumano.shared.model.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/**
 * Entidad JPA que representa a un Candidato en el sistema de selección.
 */
@Entity
@Table(
        name = "candidato",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_candidato_documento", columnNames = {"tipo_documento", "nro_documento"})
        }
)
public class Candidato extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nro_candidato", unique = true)
    private Long nroCandidato;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 10, nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "nro_documento", length = 20, nullable = false)
    private String nroDocumento;

    @Column(name = "apellido", length = 100, nullable = false)
    private String apellido;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "genero", length = 10)
    private String genero;

    @Column(name = "nacionalidad", length = 50)
    private String nacionalidad;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "escolaridad", length = 100)
    private String escolaridad;

    public Candidato() {
    }

    public Candidato(Long id, Long nroCandidato, TipoDocumento tipoDocumento, String nroDocumento,
                     String apellido, String nombre, String email) {
        this.id = id;
        this.nroCandidato = nroCandidato;
        this.tipoDocumento = tipoDocumento;
        this.nroDocumento = nroDocumento;
        this.apellido = apellido;
        this.nombre = nombre;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNroCandidato() {
        return nroCandidato;
    }

    public void setNroCandidato(Long nroCandidato) {
        this.nroCandidato = nroCandidato;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNroDocumento() {
        return nroDocumento;
    }

    public void setNroDocumento(String nroDocumento) {
        this.nroDocumento = nroDocumento;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEscolaridad() {
        return escolaridad;
    }

    public void setEscolaridad(String escolaridad) {
        this.escolaridad = escolaridad;
    }
}
