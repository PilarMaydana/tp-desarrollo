package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * DAO para el acceso a datos de la entidad {@link Resultado}.
 */
public interface ResultadoDAO extends JpaRepository<Resultado, Long> {

    Optional<Resultado> findByCuestionarioId(Long cuestionarioId);
}
