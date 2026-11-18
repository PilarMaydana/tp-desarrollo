package ar.edu.utn.frsf.g9.capitalhumano.features.auth.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para el acceso a datos de la entidad {@link Cuestionario}.
 */
@Repository
public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {

    /**
     * Busca un cuestionario perteneciente al candidato indicado, con estado dentro de la lista
     * de estados válidos y cuya clave de acceso coincida con la provista.
     *
     * @param candidato candidato asociado al cuestionario
     * @param estados lista de estados válidos (ej. ACTIVO, EN_PROCESO)
     * @param claveAcceso clave de acceso al cuestionario
     * @return cuestionario encontrado o Optional.empty()
     */
    Optional<Cuestionario> findByCandidatoAndEstadoInAndClaveAcceso(
            Candidato candidato,
            List<EstadoCuestionario> estados,
            String claveAcceso
    );
}
