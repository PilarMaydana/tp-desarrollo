package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dao.CandidatoImportRepository;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.FilaCandidatoCsvDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.ImportacionCandidatosResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.ContextoSeguridad;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.MockContextoSeguridad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para la importación masiva de candidatos (CU006 - Importar Candidatos).
 *
 * <p>Equivalente a GestorCandidatos.importarCandidatos() según el diseño funcional del sistema.</p>
 */
@Service
public class ImportarCandidatosService {

    private final CandidatoImportRepository candidatoRepository;
    private final CsvCandidatoParser csvCandidatoParser;
    private final ContextoSeguridad contextoSeguridad;

    public ImportarCandidatosService(
            CandidatoImportRepository candidatoRepository,
            CsvCandidatoParser csvCandidatoParser,
            @Autowired(required = false) ContextoSeguridad contextoSeguridad
    ) {
        this.candidatoRepository = candidatoRepository;
        this.csvCandidatoParser = csvCandidatoParser;
        this.contextoSeguridad = contextoSeguridad != null ? contextoSeguridad : new MockContextoSeguridad();
    }

    /**
     * Importa candidatos a partir de un archivo CSV en formato multipart.
     *
     * <p>El proceso se ejecuta en una única transacción atómica: si alguna fila o el archivo
     * completo contiene errores de formato/validación, se cancela la operación sin modificar la BDD.</p>
     *
     * @param file archivo CSV cargado
     * @return DTO con estadísticas de candidatos creados, actualizados y eliminados
     */
    @Transactional
    public ImportacionCandidatosResponseDTO importarCandidatos(MultipartFile file) {
        // 1. Parsear y validar todas las filas en memoria
        List<FilaCandidatoCsvDTO> filas = csvCandidatoParser.parsear(file);

        // 2. Extraer claves de candidatos y consultar en bloque a la base de datos
        Set<String> nrosCandidato = filas.stream()
                .map(FilaCandidatoCsvDTO::nroCandidato)
                .collect(Collectors.toSet());

        List<Candidato> existentes = candidatoRepository.findAllByNroCandidatoIn(nrosCandidato);
        Map<String, Candidato> mapaExistentes = existentes.stream()
                .collect(Collectors.toMap(Candidato::getNroCandidato, Function.identity()));

        // 3. Obtener identificación del usuario autenticado para auditoría de baja lógica
        Long usuarioActualId = contextoSeguridad.getUsuarioActualId();

        int creados = 0;
        int actualizados = 0;
        int eliminados = 0;

        List<Candidato> entidadesAGuardar = new ArrayList<>(filas.size());

        // 4. Procesar según la matriz de estados (Upsert + Baja Lógica)
        for (FilaCandidatoCsvDTO fila : filas) {
            Candidato candidato = mapaExistentes.get(fila.nroCandidato());

            if (candidato == null) {
                // Caso: No existe en BDD
                candidato = new Candidato();
                candidato.setNroCandidato(fila.nroCandidato());
                copiarDatosPersonales(fila, candidato);

                if (!fila.eliminado()) {
                    candidato.setEliminado(false);
                    creados++;
                } else {
                    candidato.eliminar(usuarioActualId);
                    eliminados++;
                }
            } else {
                // Caso: Existe en BDD
                copiarDatosPersonales(fila, candidato);
                boolean estabaEliminado = Boolean.TRUE.equals(candidato.getEliminado());

                if (!estabaEliminado) {
                    // Existe (activo)
                    if (!fila.eliminado()) {
                        actualizados++;
                    } else {
                        candidato.eliminar(usuarioActualId);
                        eliminados++;
                    }
                } else {
                    // Existe (eliminado previamente)
                    if (!fila.eliminado()) {
                        candidato.reactivar();
                        actualizados++;
                    } else {
                        // Mantener eliminado: datos actualizados sin sobreescribir fecha/usuario de baja
                    }
                }
            }

            entidadesAGuardar.add(candidato);
        }

        // 5. Persistir en lote
        candidatoRepository.saveAll(entidadesAGuardar);

        return new ImportacionCandidatosResponseDTO(creados, actualizados, eliminados);
    }

    private void copiarDatosPersonales(FilaCandidatoCsvDTO origen, Candidato destino) {
        destino.setApellido(origen.apellido());
        destino.setNombre(origen.nombre());
        destino.setNroDocumento(origen.nroDocumento());
        destino.setTipoDocumento(origen.tipoDocumento());
        destino.setFechaNacimiento(origen.fechaNacimiento());
        destino.setGenero(origen.genero());
        destino.setNacionalidad(origen.nacionalidad());
        destino.setEmail(origen.email());
        destino.setEscolaridad(origen.escolaridad());
    }
}
