package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA de acceso a datos de Candidato para el proceso de importación (CU006).
 */
@Repository
public interface CandidatoImportRepository extends JpaRepository<Candidato, Long> {

    /**
     * Busca un candidato por su número de candidato único.
     *
     * @param nroCandidato identificador de negocio del candidato
     * @return candidato encontrado o vacio
     */
    Optional<Candidato> findByNroCandidato(String nroCandidato);

    /**
     * Verifica si existe un candidato con el número de candidato especificado.
     *
     * @param nroCandidato identificador de negocio del candidato
     * @return true si existe, false en caso contrario
     */
    boolean existsByNroCandidato(String nroCandidato);

    /**
     * Recupera en lote todos los candidatos cuyos números figuren en la colección provista.
     *
     * @param nrosCandidato colección de números de candidatos
     * @return lista de candidatos encontrados
     */
    List<Candidato> findAllByNroCandidatoIn(Collection<String> nrosCandidato);
}
