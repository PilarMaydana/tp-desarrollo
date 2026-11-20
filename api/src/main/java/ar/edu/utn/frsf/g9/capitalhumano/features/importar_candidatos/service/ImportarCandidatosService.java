package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para la importación masiva de candidatos (CU006 - Importar Candidatos).
 *
 * <p>Identifica candidatos existentes mediante el par único {@code (tipoDocumento, nroDocumento)}
 * y asigna automáticamente el número de candidato ({@code nro_candidato}) como entero positivo
 * correlativo ({@code MAX + 1}) dentro de la transacción a los candidatos nuevos, conservando
 * el número asignado a los candidatos ya existentes.</p>
 */
@Service
public class ImportarCandidatosService {

    private static final Pattern ENTERO_POSITIVO_PATTERN = Pattern.compile("^\\d+$");

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
     * @param file archivo CSV cargado (sin columna {@code Nro candidato})
     * @return DTO con estadísticas de candidatos creados, actualizados y eliminados
     */
    @Transactional
    public ImportacionCandidatosResponseDTO importarCandidatos(MultipartFile file) {
        // 1. Parsear y validar todas las filas en memoria
        List<FilaCandidatoCsvDTO> filas = csvCandidatoParser.parsear(file);

        // 2. Consultar candidatos existentes en bloque por número de documento e indexar por (Tipo, Nro)
        Set<String> nrosDocumento = filas.stream()
                .map(FilaCandidatoCsvDTO::nroDocumento)
                .collect(Collectors.toSet());

        List<Candidato> existentes = candidatoRepository.findAllByNroDocumentoIn(nrosDocumento);
        Map<String, Candidato> mapaExistentes = new HashMap<>();
        for (Candidato existente : existentes) {
            if (existente.getTipoDocumento() != null && existente.getNroDocumento() != null) {
                String clave = construirClaveDocumento(
                        existente.getTipoDocumento(),
                        CsvCandidatoParser.normalizarNroDocumento(existente.getTipoDocumento(), existente.getNroDocumento())
                );
                mapaExistentes.put(clave, existente);
            }
        }

        // 3. Calcular máximo entero actual de nro_candidato dentro de la transacción (MAX + 1)
        long ultimoNroCandidato = calcularMaximoNroCandidato(candidatoRepository.findAllNrosCandidato());

        // 4. Obtener identificación del usuario autenticado para auditoría de baja lógica
        Long usuarioActualId = contextoSeguridad.getUsuarioActualId();

        int creados = 0;
        int actualizados = 0;
        int eliminados = 0;

        List<Candidato> entidadesAGuardar = new ArrayList<>(filas.size());

        // 5. Procesar según la matriz de estados (Upsert por Tipo+Nro documento + Baja Lógica)
        for (FilaCandidatoCsvDTO fila : filas) {
            String claveDocumento = construirClaveDocumento(fila.tipoDocumento(), fila.nroDocumento());
            Candidato candidato = mapaExistentes.get(claveDocumento);

            if (candidato == null) {
                // Caso: No existe en BDD -> asignar automáticamente MAX + 1
                candidato = new Candidato();
                candidato.setNroCandidato(String.valueOf(++ultimoNroCandidato));
                copiarDatosPersonales(fila, candidato);

                if (!fila.eliminado()) {
                    candidato.setEliminado(false);
                    creados++;
                } else {
                    candidato.eliminar(usuarioActualId);
                    eliminados++;
                }
            } else {
                // Caso: Existe en BDD -> conservar su nro_candidato original
                if (candidato.getNroCandidato() == null || candidato.getNroCandidato().isBlank()) {
                    candidato.setNroCandidato(String.valueOf(++ultimoNroCandidato));
                }
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

        // 6. Persistir en lote
        candidatoRepository.saveAll(entidadesAGuardar);

        return new ImportacionCandidatosResponseDTO(creados, actualizados, eliminados);
    }

    /**
     * Calcula el mayor entero positivo presente entre los números de candidato registrados.
     * Ignora de forma segura posibles valores heredados no numéricos para no fallar con datos legacy.
     */
    private long calcularMaximoNroCandidato(List<String> nrosCandidato) {
        if (nrosCandidato == null || nrosCandidato.isEmpty()) {
            return 0L;
        }
        long max = 0L;
        for (String nro : nrosCandidato) {
            if (nro == null) {
                continue;
            }
            String limpio = nro.trim();
            if (ENTERO_POSITIVO_PATTERN.matcher(limpio).matches()) {
                try {
                    long valor = Long.parseLong(limpio);
                    if (valor > max) {
                        max = valor;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignorar valores fuera de rango de Long
                }
            }
        }
        return max;
    }

    private String construirClaveDocumento(TipoDocumento tipoDocumento, String nroDocumentoNormalizado) {
        return tipoDocumento.name() + "::" + nroDocumentoNormalizado;
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
