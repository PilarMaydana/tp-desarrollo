package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * DAO para el acceso a datos de la entidad {@link Evaluacion}.
 */
public interface EvaluacionDAO extends JpaRepository<Evaluacion, Long> {

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
}
