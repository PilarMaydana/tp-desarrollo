package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * DAO para el acceso a datos de la entidad {@link Candidato}.
 */
public interface CandidatoDAO extends JpaRepository<Candidato, Long> {

    /**
     * Busca candidatos activos aplicando filtros opcionales "contiene" (case-insensitive)
     * por apellido, nombre y número de candidato (CU 025 - Pasos 2 y 3).
     *
     * @param apellido     fragmento opcional del apellido
     * @param nombre       fragmento opcional del nombre
     * @param nroCandidato fragmento opcional del número de candidato
     * @return lista de candidatos activos coincidentes ordenados por apellido y nombre
     */
    @Query("SELECT c FROM Candidato c " +
           "WHERE c.eliminado = false " +
           "AND (:apellido IS NULL OR UPPER(c.apellido) LIKE CONCAT('%', UPPER(:apellido), '%')) " +
           "AND (:nombre IS NULL OR UPPER(c.nombre) LIKE CONCAT('%', UPPER(:nombre), '%')) " +
           "AND (:nroCandidato IS NULL OR UPPER(COALESCE(c.nroCandidato, '')) LIKE CONCAT('%', UPPER(:nroCandidato), '%')) " +
           "ORDER BY c.apellido ASC, c.nombre ASC, c.id ASC")
    List<Candidato> buscarActivosConFiltros(
            @Param("apellido") String apellido,
            @Param("nombre") String nombre,
            @Param("nroCandidato") String nroCandidato
    );

    /**
     * Obtiene todos los candidatos activos cuyos identificadores pertenecen a la colección dada.
     *
     * @param ids colección de IDs de candidatos
     * @return lista de candidatos activos encontrados
     */
    List<Candidato> findByIdInAndEliminadoFalse(Collection<Long> ids);
}
