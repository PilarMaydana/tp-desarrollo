package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para la transferencia de datos en la búsqueda, alta y modificación de Preguntas (CU 020).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreguntaDTO {

    private Long id;

    private String competencia;

    private String factor;

    @JsonAlias({"factor_codigo", "codigoFactor"})
    private String factorCodigo;

    @JsonAlias({"factor_id"})
    private Long factorId;

    @NotBlank(message = "El nombre de la pregunta es obligatorio")
    @Size(max = 255, message = "El nombre de la pregunta no puede superar los 255 caracteres")
    private String nombre;

    @NotBlank(message = "El enunciado de la pregunta es obligatorio")
    @Size(max = 1000, message = "El enunciado de la pregunta no puede superar los 1000 caracteres")
    private String enunciado;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @NotNull(message = "La pregunta debe tener opciones de respuesta")
    @Size(min = 2, message = "La pregunta debe tener al menos 2 opciones de respuesta")
    @Valid
    private List<OpcionRespuestaDTO> opciones = new ArrayList<>();

    public PreguntaDTO() {
    }

    public PreguntaDTO(Long id, String competencia, String factor, String nombre, String enunciado, String descripcion, List<OpcionRespuestaDTO> opciones) {
        this.id = id;
        this.competencia = competencia;
        this.factor = factor;
        this.nombre = nombre;
        this.enunciado = enunciado;
        this.descripcion = descripcion;
        this.opciones = opciones != null ? opciones : new ArrayList<>();
    }

    public PreguntaDTO(Long id, String competencia, String factor, String factorCodigo, String nombre, String enunciado, String descripcion, List<OpcionRespuestaDTO> opciones) {
        this.id = id;
        this.competencia = competencia;
        this.factor = factor;
        this.factorCodigo = factorCodigo;
        this.nombre = nombre;
        this.enunciado = enunciado;
        this.descripcion = descripcion;
        this.opciones = opciones != null ? opciones : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompetencia() {
        return competencia;
    }

    public void setCompetencia(String competencia) {
        this.competencia = competencia;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public String getFactorCodigo() {
        return factorCodigo;
    }

    public void setFactorCodigo(String factorCodigo) {
        this.factorCodigo = factorCodigo;
    }

    public Long getFactorId() {
        return factorId;
    }

    public void setFactorId(Long factorId) {
        this.factorId = factorId;
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

    public List<OpcionRespuestaDTO> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionRespuestaDTO> opciones) {
        this.opciones = opciones != null ? opciones : new ArrayList<>();
    }
}

