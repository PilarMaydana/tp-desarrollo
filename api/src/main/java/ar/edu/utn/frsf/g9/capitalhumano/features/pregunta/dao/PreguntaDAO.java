package ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * DAO para el acceso a datos de la entidad {@link Pregunta}.
 */
public interface PreguntaDAO extends JpaRepository<Pregunta, Long> {

    /**
     * Verifica si ya existe una pregunta con el mismo nombre para el factor dado.
     *
     * @param factor entidad factor
     * @param nombre nombre normalizado de la pregunta
     * @return true si ya existe, false si no
     */
    boolean existsByFactorAndNombre(Factor factor, String nombre);

    /**
     * Verifica si ya existe una pregunta con el mismo enunciado para el factor dado.
     *
     * @param factor entidad factor
     * @param enunciado enunciado normalizado de la pregunta
     * @return true si ya existe, false si no
     */
    boolean existsByFactorAndEnunciado(Factor factor, String enunciado);

    /**
     * Verifica si ya existe una pregunta con el mismo nombre para el código de factor dado.
     *
     * @param codigoFactor código del factor
     * @param nombre nombre normalizado de la pregunta
     * @return true si ya existe, false si no
     */
    boolean existsByFactorCodigoAndNombre(String codigoFactor, String nombre);

    /**
     * Verifica si ya existe una pregunta con el mismo enunciado para el código de factor dado.
     *
     * @param codigoFactor código del factor
     * @param enunciado enunciado normalizado de la pregunta
     * @return true si ya existe, false si no
     */
    boolean existsByFactorCodigoAndEnunciado(String codigoFactor, String enunciado);

    /**
     * Obtiene todas las preguntas asociadas al código de factor indicado.
     *
     * @param codigoFactor código del factor
     * @return lista de preguntas
     */
    List<Pregunta> findByFactorCodigo(String codigoFactor);
}
