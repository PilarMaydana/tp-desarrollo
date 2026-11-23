package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * DAO para el acceso a datos de la entidad {@link Evaluacion}.
 */
public interface EvaluacionDAO extends JpaRepository<Evaluacion, Long> {

    /**
     * Proyección agregada por evaluación para evitar consultas N+1 al listar puestos
     * y evaluaciones en los reportes (CU 028).
     */
    interface ResumenEvaluacionProjection {
        Long getEvaluacionId();

        Long getPuestoId();

        LocalDateTime getFechaCreacion();

        Long getCantidadCandidatos();

        Long getCantidadCompletos();
    }

    /**
     * Obtiene una evaluación con su puesto, cuestionarios y candidatos asociados cargados.
     *
     * @param id identificador de la evaluación
     * @return evaluación encontrada o vacío
     */
    @Query("SELECT DISTINCT e FROM Evaluacion e " +
           "JOIN FETCH e.puesto p " +
           "LEFT JOIN FETCH e.cuestionarios cu " +
           "LEFT JOIN FETCH cu.candidato cand " +
           "WHERE e.id = :id")
    Optional<Evaluacion> findByIdConCuestionariosYCandidatos(@Param("id") Long id);

    /**
     * Obtiene en una única consulta agregada el resumen de evaluaciones (cantidad de candidatos
     * y cantidad de cuestionarios en estado COMPLETO) para uno o más puestos, ordenadas de más
     * reciente a más antigua (CU 028).
     *
     * @param puestoIds      identificadores de los puestos a consultar
     * @param estadoCompleto estado {@link EstadoCuestionario#COMPLETO}
     * @return lista de proyecciones agregadas por evaluación
     */
    @Query("SELECT e.id AS evaluacionId, " +
           "e.puesto.id AS puestoId, " +
           "e.fechaCreacion AS fechaCreacion, " +
           "COUNT(c.id) AS cantidadCandidatos, " +
           "COALESCE(SUM(CASE WHEN c.estado = :estadoCompleto THEN 1L ELSE 0L END), 0L) AS cantidadCompletos " +
           "FROM Evaluacion e " +
           "LEFT JOIN e.cuestionarios c ON c.eliminado = false " +
           "WHERE e.puesto.id IN :puestoIds " +
           "GROUP BY e.id, e.puesto.id, e.fechaCreacion " +
           "ORDER BY e.fechaCreacion DESC, e.id DESC")
    List<ResumenEvaluacionProjection> findResumenPorPuestoIds(
            @Param("puestoIds") Collection<Long> puestoIds,
            @Param("estadoCompleto") EstadoCuestionario estadoCompleto
    );

    /**
     * Lista las evaluaciones de un puesto ordenadas de más reciente a más antigua.
     *
     * @param puestoId identificador del puesto
     * @return evaluaciones del puesto
     */
    List<Evaluacion> findByPuestoIdOrderByFechaCreacionDescIdDesc(Long puestoId);
}
