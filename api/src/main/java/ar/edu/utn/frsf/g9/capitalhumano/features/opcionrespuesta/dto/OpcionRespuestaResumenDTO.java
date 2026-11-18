package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resumen para el listado y búsqueda de Opciones de Respuesta (CU 024).
 */
@Schema(name = "OpcionRespuestaResumenDTO", description = "Resumen de una opción de respuesta (sin ítems) para listados y búsquedas (CU 024)")
public class OpcionRespuestaResumenDTO {

    @Schema(description = "Identificador único de la opción de respuesta", example = "1")
    private Long id;

    @Schema(description = "Nombre único de la opción de respuesta", example = "FRECUENCIA")
    private String nombre;

    @Schema(description = "Descripción de la opción de respuesta", example = "ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO")
    private String descripcion;

    public OpcionRespuestaResumenDTO() {
    }

    public OpcionRespuestaResumenDTO(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
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
}
