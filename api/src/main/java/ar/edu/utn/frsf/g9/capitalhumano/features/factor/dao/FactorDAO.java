package ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * DAO para el acceso a datos de la entidad {@link Factor} (CU 017).
 */
public interface FactorDAO extends JpaRepository<Factor, Long> {

    /**
     * Verifica si ya existe un factor registrado con el código especificado.
     *
     * @param codigo código normalizado en mayúsculas
     * @return {@code true} si existe, {@code false} en caso contrario
     */
    boolean existsByCodigo(String codigo);

    /**
     * Verifica si ya existe un factor registrado con el nombre especificado.
     *
     * @param nombre nombre normalizado en mayúsculas
     * @return {@code true} si existe, {@code false} en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Verifica si ya existe un factor registrado con el mismo número de orden para una competencia dada.
     *
     * @param competenciaId identificador de la competencia
     * @param nroOrden número de orden a verificar
     * @return {@code true} si ya existe dicho orden en la competencia, {@code false} en caso contrario
     */
    boolean existsByCompetenciaIdAndNroOrden(Long competenciaId, Integer nroOrden);

    /**
     * Obtiene los factores asociados a una competencia ordenados por número de orden ascendente.
     *
     * @param competenciaId identificador de la competencia
     * @return lista de factores asociados
     */
    List<Factor> findByCompetenciaIdOrderByNroOrdenAsc(Long competenciaId);
}

