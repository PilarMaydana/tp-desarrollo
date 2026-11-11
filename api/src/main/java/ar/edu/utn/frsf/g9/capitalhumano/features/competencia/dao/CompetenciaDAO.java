package ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * DAO para el acceso a datos de la entidad {@link Competencia}.
 */
public interface CompetenciaDAO extends JpaRepository<Competencia, Long> {

    /**
     * Verifica si ya existe una competencia con el código indicado.
     *
     * @param codigo código de la competencia (normalizado en mayúsculas)
     * @return {@code true} si existe, {@code false} en caso contrario
     */
    boolean existsByCodigo(String codigo);

    /**
     * Verifica si ya existe una competencia con el nombre indicado.
     *
     * @param nombre nombre de la competencia (normalizado en mayúsculas)
     * @return {@code true} si existe, {@code false} en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Busca una competencia por su código.
     *
     * @param codigo código de la competencia
     * @return competencia encontrada o empty
     */
    Optional<Competencia> findByCodigo(String codigo);

    /**
     * Busca todas las competencias que no han sido eliminadas lógicamente.
     *
     * @return lista de competencias activas
     */
    List<Competencia> findByEliminadoFalse();
}
