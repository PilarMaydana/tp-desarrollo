package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.model.EntidadAuditable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa un Cuestionario asignado a un candidato para un proceso de evaluación.
 */
@Entity
@Table(name = "cuestionario")
public class Cuestionario extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidato_id", nullable = false)
    private Candidato candidato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id")
    private Puesto puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluacion_id")
    private Evaluacion evaluacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoCuestionario estado;

    @Column(name = "clave_acceso", length = 8, nullable = false)
    private String claveAcceso;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_cambio_estado")
    private LocalDateTime fechaCambioEstado;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @Column(name = "cantidad_accesos", nullable = false)
    private Integer cantidadAccesos = 0;

    @Column(name = "tiempo_maximo_minutos")
    private Integer tiempoMaximoMinutos;

    @Column(name = "cantidad_preguntas_por_bloque")
    private Integer cantidadPreguntasPorBloque;

    @Column(name = "tiempo_restante_segundos")
    private Integer tiempoRestanteSegundos;

    @Column(name = "ultimo_bloque_respondido")
    private Integer ultimoBloqueRespondido;

    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nroBloque ASC")
    private List<BloqueCuestionario> bloques = new ArrayList<>();

    public Cuestionario() {
    }

    public Cuestionario(Long id, Candidato candidato, EstadoCuestionario estado, String claveAcceso) {
        this.id = id;
        this.candidato = candidato;
        this.estado = estado;
        this.claveAcceso = claveAcceso;
        LocalDateTime ahora = LocalDateTime.now();
        this.fechaCreacion = ahora;
        this.fechaCambioEstado = ahora;
        this.cantidadAccesos = 0;
    }

    /**
     * Divide las preguntas seleccionadas en instancias de {@link BloqueCuestionario}
     * numeradas secuencialmente {@code 1..N} y crea sus {@link PreguntaAsignada}
     * con {@code nroOrden} secuencial, respetando {@link #cantidadPreguntasPorBloque}.
     *
     * @param preguntas lista ordenada/mezclada de preguntas seleccionadas para el cuestionario
     * @throws IllegalStateException si {@code cantidadPreguntasPorBloque} no es mayor a 0
     */
    public void armarBloques(List<Pregunta> preguntas) {
        if (cantidadPreguntasPorBloque == null || cantidadPreguntasPorBloque <= 0) {
            throw new IllegalStateException("La cantidad de preguntas por bloque debe ser mayor a 0");
        }
        this.bloques.clear();
        if (preguntas == null || preguntas.isEmpty()) {
            return;
        }

        int nroBloque = 1;
        int nroOrden = 1;
        for (int i = 0; i < preguntas.size(); i += cantidadPreguntasPorBloque) {
            int fin = Math.min(i + cantidadPreguntasPorBloque, preguntas.size());
            BloqueCuestionario bloque = new BloqueCuestionario(nroBloque++);
            for (int j = i; j < fin; j++) {
                bloque.addPreguntaAsignada(new PreguntaAsignada(preguntas.get(j), nroOrden++));
            }
            addBloque(bloque);
        }
    }

    public void addBloque(BloqueCuestionario bloque) {
        if (bloque != null) {
            bloques.add(bloque);
            bloque.setCuestionario(this);
        }
    }

    public int getTotalPreguntasAsignadas() {
        return bloques.stream()
                .mapToInt(b -> b.getPreguntasAsignadas().size())
                .sum();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Candidato getCandidato() {
        return candidato;
    }

    public void setCandidato(Candidato candidato) {
        this.candidato = candidato;
    }

    public Puesto getPuesto() {
        return puesto;
    }

    public void setPuesto(Puesto puesto) {
        this.puesto = puesto;
    }

    public Long getPuestoId() {
        return puesto != null ? puesto.getId() : null;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(Evaluacion evaluacion) {
        this.evaluacion = evaluacion;
    }

    public EstadoCuestionario getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuestionario estado) {
        this.estado = estado;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaCambioEstado() {
        return fechaCambioEstado;
    }

    public void setFechaCambioEstado(LocalDateTime fechaCambioEstado) {
        this.fechaCambioEstado = fechaCambioEstado;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public Integer getCantidadAccesos() {
        return cantidadAccesos;
    }

    public void setCantidadAccesos(Integer cantidadAccesos) {
        this.cantidadAccesos = cantidadAccesos != null ? cantidadAccesos : 0;
    }

    public Integer getTiempoMaximoMinutos() {
        return tiempoMaximoMinutos;
    }

    public void setTiempoMaximoMinutos(Integer tiempoMaximoMinutos) {
        this.tiempoMaximoMinutos = tiempoMaximoMinutos;
    }

    public Integer getCantidadPreguntasPorBloque() {
        return cantidadPreguntasPorBloque;
    }

    public void setCantidadPreguntasPorBloque(Integer cantidadPreguntasPorBloque) {
        this.cantidadPreguntasPorBloque = cantidadPreguntasPorBloque;
    }

    public Integer getTiempoRestanteSegundos() {
        return tiempoRestanteSegundos;
    }

    public void setTiempoRestanteSegundos(Integer tiempoRestanteSegundos) {
        this.tiempoRestanteSegundos = tiempoRestanteSegundos;
    }

    public Integer getUltimoBloqueRespondido() {
        return ultimoBloqueRespondido;
    }

    public void setUltimoBloqueRespondido(Integer ultimoBloqueRespondido) {
        this.ultimoBloqueRespondido = ultimoBloqueRespondido;
    }

    public List<BloqueCuestionario> getBloques() {
        return bloques;
    }

    public void setBloques(List<BloqueCuestionario> bloques) {
        this.bloques = bloques != null ? bloques : new ArrayList<>();
    }
}
