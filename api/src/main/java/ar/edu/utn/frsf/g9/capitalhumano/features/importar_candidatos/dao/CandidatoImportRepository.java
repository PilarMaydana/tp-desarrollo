package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
     * @param nroCandidato identificador numérico de negocio del candidato
     * @return candidato encontrado o vacío
     */
    Optional<Candidato> findByNroCandidato(String nroCandidato);

    /**
     * Recupera en lote todos los candidatos cuyos números de documento figuren en la colección provista.
     *
     * @param nrosDocumento colección de números de documento normalizados
     * @return lista de candidatos coincidentes (luego filtrados por par tipo + número de documento)
     */
    List<Candidato> findAllByNroDocumentoIn(Collection<String> nrosDocumento);

    /**
     * Obtiene todos los valores actuales de {@code nro_candidato} no nulos para calcular
     * de forma segura y portable (H2 y PostgreSQL) el siguiente número correlativo {@code MAX + 1}.
     *
     * @return listado de números de candidato registrados
     */
    @Query("SELECT c.nroCandidato FROM Candidato c WHERE c.nroCandidato IS NOT NULL")
    List<String> findAllNrosCandidato();
}
