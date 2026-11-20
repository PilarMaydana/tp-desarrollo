package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.RespuestaCandidato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * DAO para el acceso a datos de la entidad {@link RespuestaCandidato}.
 */
public interface RespuestaCandidatoDAO extends JpaRepository<RespuestaCandidato, Long> {

    Optional<RespuestaCandidato> findByPreguntaAsignadaId(Long preguntaAsignadaId);
}
