package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * DAO para el acceso a datos de la entidad {@link Cuestionario}.
 */
public interface CuestionarioDAO extends JpaRepository<Cuestionario, Long> {

    /**
     * Busca cuestionarios no eliminados de los candidatos indicados cuyo estado esté incluido
     * en la colección de estados especificada (ej. ACTIVO, EN_PROCESO).
     *
     * @param candidatoIds identificadores de los candidatos a verificar
     * @param estados      estados a filtrar
     * @return lista de cuestionarios coincidentes con su candidato cargado
     */
    @Query("SELECT c FROM Cuestionario c " +
           "JOIN FETCH c.candidato cand " +
           "WHERE cand.id IN :candidatoIds " +
           "AND c.estado IN :estados " +
           "AND c.eliminado = false " +
           "ORDER BY cand.apellido ASC, cand.nombre ASC")
    List<Cuestionario> findActivosByCandidatoIdInAndEstadoIn(
            @Param("candidatoIds") Collection<Long> candidatoIds,
            @Param("estados") Collection<EstadoCuestionario> estados
    );
}
