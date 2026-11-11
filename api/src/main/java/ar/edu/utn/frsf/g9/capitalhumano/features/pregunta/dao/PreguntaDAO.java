package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * DAO para el acceso a datos y consultas de la entidad {@link Pregunta} (CU 020 / CU 021).
 */
public interface PreguntaDAO extends JpaRepository<Pregunta, Long> {

    /**
     * Busca preguntas activas aplicando los criterios opcionales con semántica "contiene".
     *
     * @param competencia fragmento a buscar en código o nombre de la competencia (opcional)
     * @param factor fragmento a buscar en código o nombre del factor (opcional)
     * @param nombre fragmento a buscar en el nombre o enunciado de la pregunta (opcional)
     * @return lista de preguntas coincidentes con sus relaciones cargadas
     */
    @Query("SELECT DISTINCT p FROM Pregunta p " +
           "JOIN FETCH p.factor f " +
           "JOIN FETCH f.competencia c " +
           "LEFT JOIN FETCH p.opciones o " +
           "WHERE p.eliminado = false " +
           "AND (:competencia IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :competencia, '%')) OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :competencia, '%'))) " +
           "AND (:factor IS NULL OR LOWER(f.nombre) LIKE LOWER(CONCAT('%', :factor, '%')) OR LOWER(f.codigo) LIKE LOWER(CONCAT('%', :factor, '%'))) " +
           "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(p.enunciado) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "ORDER BY p.id ASC")
    List<Pregunta> buscarConFiltros(@Param("competencia") String competencia,
                                    @Param("factor") String factor,
                                    @Param("nombre") String nombre);

    /**
     * Busca una pregunta activa por ID con sus opciones, factor y competencia.
     *
     * @param id identificador de la pregunta
     * @return pregunta encontrada o vacío
     */
    @Query("SELECT DISTINCT p FROM Pregunta p " +
           "JOIN FETCH p.factor f " +
           "JOIN FETCH f.competencia c " +
           "LEFT JOIN FETCH p.opciones o " +
           "WHERE p.id = :id AND p.eliminado = false")
    Optional<Pregunta> findByIdAndEliminadoFalse(@Param("id") Long id);

    /**
     * Verifica si existe una pregunta activa en un factor con el nombre indicado (ignorando mayúsculas).
     */
    boolean existsByFactorIdAndNombreIgnoreCaseAndEliminadoFalse(Long factorId, String nombre);

    /**
     * Verifica si existe una pregunta activa en un factor con el enunciado indicado (ignorando mayúsculas).
     */
    boolean existsByFactorIdAndEnunciadoIgnoreCaseAndEliminadoFalse(Long factorId, String enunciado);

    /**
     * Verifica si existe otra pregunta activa en el factor con el mismo nombre (para modificación).
     */
    boolean existsByFactorIdAndNombreIgnoreCaseAndIdNotAndEliminadoFalse(Long factorId, String nombre, Long id);

    /**
     * Verifica si existe otra pregunta activa en el factor con el mismo enunciado (para modificación).
     */
    boolean existsByFactorIdAndEnunciadoIgnoreCaseAndIdNotAndEliminadoFalse(Long factorId, String enunciado, Long id);

    /**
     * Métodos adicionales para compatibilidad con CU 021
     */
    boolean existsByFactorAndNombre(Factor factor, String nombre);

    boolean existsByFactorAndEnunciado(Factor factor, String enunciado);

    boolean existsByFactorCodigoAndNombre(String codigoFactor, String nombre);

    boolean existsByFactorCodigoAndEnunciado(String codigoFactor, String enunciado);

    List<Pregunta> findByFactorCodigo(String codigoFactor);
}
