package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * DAO para el acceso a datos de la entidad {@link Resultado}.
 */
public interface ResultadoDAO extends JpaRepository<Resultado, Long> {

    Optional<Resultado> findByCuestionarioId(Long cuestionarioId);

    /**
     * Obtiene los resultados de un conjunto de cuestionarios junto con sus puntajes por
     * competencia y las competencias asociadas en una única consulta (evita N+1 en CU 028).
     *
     * @param cuestionarioIds identificadores de cuestionarios en estado COMPLETO
     * @return lista de resultados con sus {@code resultadosCompetencias} y {@code competencia} precargados
     */
    @Query("SELECT DISTINCT r FROM Resultado r " +
           "JOIN FETCH r.cuestionario c " +
           "LEFT JOIN FETCH r.resultadosCompetencias rc " +
           "LEFT JOIN FETCH rc.competencia comp " +
           "WHERE c.id IN :cuestionarioIds")
    List<Resultado> findByCuestionarioIdInConCompetencias(
            @Param("cuestionarioIds") Collection<Long> cuestionarioIds
    );
}
