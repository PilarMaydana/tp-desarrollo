package ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.opcionrespuesta.model.OpcionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * DAO para el acceso a datos de la entidad {@link OpcionRespuesta} (CU 024 / CU 029).
 */
public interface OpcionRespuestaDAO extends JpaRepository<OpcionRespuesta, Long> {

    /**
     * Verifica si ya existe una opción de respuesta con el nombre indicado.
     *
     * @param nombre nombre normalizado a verificar
     * @return {@code true} si ya existe, {@code false} en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Obtiene todas las opciones de respuesta ordenadas por nombre ascendente (CU 024).
     *
     * @return lista completa de opciones de respuesta ordenada alfabéticamente por nombre
     */
    List<OpcionRespuesta> findAllByOrderByNombreAsc();

    /**
     * Busca opciones de respuesta cuyo nombre contenga el texto indicado (sin distinguir
     * mayúsculas/minúsculas), ordenadas por nombre ascendente (CU 024).
     *
     * <p>Al ser un método derivado de Spring Data JPA con {@code Containing}, los caracteres
     * comodín de LIKE ({@code %} y {@code _}) presentes en el parámetro son escapados
     * automáticamente y tratados como caracteres literales.</p>
     *
     * @param nombre fragmento de nombre a buscar
     * @return lista de opciones de respuesta coincidentes ordenada alfabéticamente por nombre
     */
    List<OpcionRespuesta> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);
}

