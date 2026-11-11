package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;

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
}

