package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    /**
     * Busca un cuestionario no eliminado por su ID verificando que pertenezca al candidato indicado,
     * cargando su puesto y evaluación asociados.
     *
     * @param id          identificador del cuestionario
     * @param candidatoId identificador del candidato dueño del cuestionario
     * @return cuestionario encontrado o vacío si no existe o pertenece a otro candidato
     */
    @Query("SELECT c FROM Cuestionario c " +
           "LEFT JOIN FETCH c.puesto p " +
           "LEFT JOIN FETCH c.evaluacion e " +
           "LEFT JOIN FETCH e.puesto ep " +
           "WHERE c.id = :id " +
           "AND c.candidato.id = :candidatoId " +
           "AND c.eliminado = false")
    Optional<Cuestionario> findByIdAndCandidatoIdConPuesto(
            @Param("id") Long id,
            @Param("candidatoId") Long candidatoId
    );
}
