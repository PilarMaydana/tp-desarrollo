package ar.edu.utn.frsf.g9.capitalhumano.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

/**
 * Superclase para entidades que requieren seguimiento de auditoría y soporte de baja lógica.
 */
@MappedSuperclass
public abstract class EntidadAuditable {

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = Boolean.FALSE;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    @Column(name = "usuario_eliminacion")
    private Long usuarioEliminacion;

    public EntidadAuditable() {
    }

    public Boolean isEliminado() {
        return eliminado;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado != null ? eliminado : Boolean.FALSE;
    }

    public LocalDateTime getFechaEliminacion() {
        return fechaEliminacion;
    }

    public void setFechaEliminacion(LocalDateTime fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;
    }

    public Long getUsuarioEliminacion() {
        return usuarioEliminacion;
    }

    public void setUsuarioEliminacion(Long usuarioEliminacion) {
        this.usuarioEliminacion = usuarioEliminacion;
    }
}

