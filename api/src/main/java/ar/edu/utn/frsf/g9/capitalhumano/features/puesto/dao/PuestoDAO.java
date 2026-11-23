package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * DAO para el acceso a datos de la entidad {@link Puesto}.
 */
public interface PuestoDAO extends JpaRepository<Puesto, Long> {

    /**
     * Verifica si ya existe un puesto con el código especificado.
     *
     * @param codigo código del puesto normalizado
     * @return {@code true} si existe, {@code false} de lo contrario
     */
    boolean existsByCodigo(String codigo);

    /**
     * Verifica si ya existe un puesto con el nombre especificado.
     *
     * @param nombre nombre del puesto normalizado
     * @return {@code true} si existe, {@code false} de lo contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Busca un puesto por su código.
     *
     * @param codigo código del puesto normalizado
     * @return Optional con el puesto encontrado, o vacío
     */
    Optional<Puesto> findByCodigo(String codigo);

    /**
     * Busca un puesto por su nombre.
     *
     * @param nombre nombre del puesto normalizado
     * @return Optional con el puesto encontrado, o vacío
     */
    Optional<Puesto> findByNombre(String nombre);

    /**
     * Lista todos los puestos activos ordenados alfabéticamente por nombre.
     *
     * @return lista de puestos activos
     */
    List<Puesto> findByEliminadoFalseOrderByNombreAsc();

    /**
     * Obtiene un puesto activo por su ID con sus características y competencias asociadas cargadas.
     *
     * @param id identificador del puesto
     * @return Optional con el puesto encontrado, o vacío
     */
    @Query("SELECT DISTINCT p FROM Puesto p " +
           "LEFT JOIN FETCH p.caracteristicas cp " +
           "LEFT JOIN FETCH cp.competencia c " +
           "WHERE p.id = :id AND p.eliminado = false")
    Optional<Puesto> findByIdActivaConCaracteristicas(@Param("id") Long id);

    /**
     * Verifica si existe un puesto activo (no eliminado) con el ID especificado.
     *
     * @param id identificador del puesto
     * @return {@code true} si existe y está activo
     */
    boolean existsByIdAndEliminadoFalse(Long id);

    /**
     * Busca puestos activos aplicando criterios opcionales "contiene" (case-insensitive)
     * sobre código, nombre y empresa, ordenados alfabéticamente por nombre (CU 028).
     *
     * @param codigo  fragmento normalizado (en mayúsculas) o {@code null}
     * @param nombre  fragmento normalizado (en mayúsculas) o {@code null}
     * @param empresa fragmento normalizado (en mayúsculas) o {@code null}
     * @return lista de puestos coincidentes ordenados por nombre ascendente
     */
    @Query("SELECT p FROM Puesto p " +
           "WHERE p.eliminado = false " +
           "AND (:codigo IS NULL OR UPPER(p.codigo) LIKE CONCAT('%', :codigo, '%')) " +
           "AND (:nombre IS NULL OR UPPER(p.nombre) LIKE CONCAT('%', :nombre, '%')) " +
           "AND (:empresa IS NULL OR UPPER(p.empresa) LIKE CONCAT('%', :empresa, '%')) " +
           "ORDER BY p.nombre ASC, p.id ASC")
    List<Puesto> buscarActivosPorCriterios(
            @Param("codigo") String codigo,
            @Param("nombre") String nombre,
            @Param("empresa") String empresa
    );
}

