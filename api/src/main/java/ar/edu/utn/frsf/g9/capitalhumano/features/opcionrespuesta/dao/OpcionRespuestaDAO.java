package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.model.OpcionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO para el acceso a datos de la entidad {@link OpcionRespuesta} (CU 029).
 */
public interface OpcionRespuestaDAO extends JpaRepository<OpcionRespuesta, Long> {

    /**
     * Verifica si ya existe una opción de respuesta con el nombre indicado.
     *
     * @param nombre nombre normalizado a verificar
     * @return {@code true} si ya existe, {@code false} en caso contrario
     */
    boolean existsByNombre(String nombre);
}

