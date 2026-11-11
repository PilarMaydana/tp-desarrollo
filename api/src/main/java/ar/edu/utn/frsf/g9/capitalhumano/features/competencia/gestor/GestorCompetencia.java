package ar.edu.utn.frsf.g9.capitalhumano.features.competencia.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dao.CompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.dto.CompetenciaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoDuplicadoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Gestor de lógica de negocio para la administración de Competencias (CU 013).
 */
@Service
public class GestorCompetencia {

    private final CompetenciaDAO competenciaDAO;

    public GestorCompetencia(CompetenciaDAO competenciaDAO) {
        this.competenciaDAO = competenciaDAO;
    }

    /**
     * Da de alta una nueva competencia en el sistema.
     *
     * <p>Normaliza los campos literales aplicando {@code trim()} y convirtiéndolos
     * a mayúsculas antes de verificar duplicados y persistir la entidad.</p>
     *
     * @param dto datos de entrada de la competencia a crear
     * @return DTO con los datos de la competencia persistida (incluyendo su ID generado)
     * @throws RecursoDuplicadoException si ya existe una competencia con el mismo código o nombre
     */
    @Transactional
    public CompetenciaDTO crearCompetencia(CompetenciaDTO dto) {
        String codigo = dto.getCodigo().trim().toUpperCase(Locale.ROOT);
        String nombre = dto.getNombre().trim().toUpperCase(Locale.ROOT);
        String descripcion = dto.getDescripcion().trim().toUpperCase(Locale.ROOT);

        if (competenciaDAO.existsByCodigo(codigo)) {
            throw new RecursoDuplicadoException("Ya existe una competencia con el código '" + codigo + "'");
        }

        if (competenciaDAO.existsByNombre(nombre)) {
            throw new RecursoDuplicadoException("Ya existe una competencia con el nombre '" + nombre + "'");
        }

        Competencia entidad = aEntidad(codigo, nombre, descripcion);
        Competencia guardada = competenciaDAO.save(entidad);
        return aDTO(guardada);
    }

    /**
     * Obtiene el listado completo de competencias registradas en el sistema,
     * ordenadas por nombre de forma ascendente.
     *
     * @return lista de competencias en formato DTO ordenada por nombre ascendente
     */
    @Transactional(readOnly = true)
    public List<CompetenciaDTO> listar() {
        return competenciaDAO.findAll().stream()
                .sorted(Comparator.comparing(
                        Competencia::getNombre,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .map(this::aDTO)
                .toList();
    }

    /**
     * Alias de {@link #listar()} para compatibilidad.
     *
     * @return lista de competencias en formato DTO ordenada por nombre ascendente
     */
    @Transactional(readOnly = true)
    public List<CompetenciaDTO> listarCompetencias() {
        return listar();
    }

    private Competencia aEntidad(String codigo, String nombre, String descripcion) {
        Competencia competencia = new Competencia();
        competencia.setCodigo(codigo);
        competencia.setNombre(nombre);
        competencia.setDescripcion(descripcion);
        return competencia;
    }

    private CompetenciaDTO aDTO(Competencia entidad) {
        return new CompetenciaDTO(
                entidad.getId(),
                entidad.getCodigo(),
                entidad.getNombre(),
                entidad.getDescripcion()
        );
    }
}
