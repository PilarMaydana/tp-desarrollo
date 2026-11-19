package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Genero;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;

import java.time.LocalDate;

/**
 * Representación tipada de los datos validados de una fila del archivo CSV de candidatos.
 */
public record FilaCandidatoCsvDTO(
        int nroFila,
        String nroCandidato,
        String apellido,
        String nombre,
        String nroDocumento,
        TipoDocumento tipoDocumento,
        LocalDate fechaNacimiento,
        Genero genero,
        String nacionalidad,
        String email,
        String escolaridad,
        boolean eliminado
) {
}
