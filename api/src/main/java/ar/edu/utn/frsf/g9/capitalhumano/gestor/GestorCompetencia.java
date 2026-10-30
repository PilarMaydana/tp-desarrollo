package ar.edu.utn.frsf.g9.capitalhumano.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.dao.CompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.dto.CompetenciaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.exception.RecursoDuplicadoException;
import ar.edu.utn.frsf.g9.capitalhumano.model.Competencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
