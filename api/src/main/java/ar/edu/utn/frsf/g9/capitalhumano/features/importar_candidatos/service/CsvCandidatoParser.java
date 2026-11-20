package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Genero;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Nacionalidad;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.escolaridad.Escolaridad;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.ErrorImportacionDTO.DetalleErrorFilaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.FilaCandidatoCsvDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.exception.CsvImportacionException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Componente encargado del parseo y validación exhaustiva del archivo CSV de candidatos (CU006).
 *
 * <p>El archivo CSV no debe incluir la columna {@code Nro candidato}, ya que el número de candidato
 * es asignado y mantenido automáticamente por el sistema. La unicidad dentro del archivo se controla
 * mediante la clave compuesta {@code (Tipo documento, Nro documento)} normalizada.</p>
 */
@Component
public class CsvCandidatoParser {

    private static final String SEPARADOR = ";";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern SOLO_DIGITOS_PATTERN = Pattern.compile("^\\d+$");
    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final String COL_NRO_CANDIDATO = "NRO_CANDIDATO";
    private static final String COL_APELLIDO = "APELLIDO";
    private static final String COL_NOMBRE = "NOMBRE";
    private static final String COL_NRO_DOCUMENTO = "NRO_DOCUMENTO";
    private static final String COL_TIPO_DOCUMENTO = "TIPO_DOCUMENTO";
    private static final String COL_FECHA_NACIMIENTO = "FECHA_NACIMIENTO";
    private static final String COL_GENERO = "GENERO";
    private static final String COL_NACIONALIDAD = "NACIONALIDAD";
    private static final String COL_EMAIL = "EMAIL";
    private static final String COL_ESCOLARIDAD = "ESCOLARIDAD";
    private static final String COL_ELIMINADO = "ELIMINADO";

    private static final List<String> COLUMNAS_REQUERIDAS = List.of(
            COL_APELLIDO,
            COL_NOMBRE,
            COL_NRO_DOCUMENTO,
            COL_TIPO_DOCUMENTO,
            COL_FECHA_NACIMIENTO,
            COL_GENERO,
            COL_NACIONALIDAD,
            COL_EMAIL,
            COL_ESCOLARIDAD,
            COL_ELIMINADO
    );

    /**
     * Normaliza un número de documento según su tipo:
     * <ul>
     *   <li>Para {@code DNI}, {@code LE} y {@code LC}: elimina puntos y espacios y pasa a mayúsculas.</li>
     *   <li>Para {@code PP}: elimina espacios y pasa a mayúsculas.</li>
     * </ul>
     *
     * @param tipoDocumento tipo de documento (puede ser {@code null} si aún no fue validado)
     * @param nroDocumento  valor crudo recibido
     * @return número de documento normalizado
     */
    public static String normalizarNroDocumento(TipoDocumento tipoDocumento, String nroDocumento) {
        if (nroDocumento == null) {
            return "";
        }
        String upper = nroDocumento.trim().toUpperCase(Locale.ROOT);
        if (tipoDocumento == TipoDocumento.PP) {
            return upper.replaceAll("\\s+", "");
        }
        if (tipoDocumento == TipoDocumento.DNI
                || tipoDocumento == TipoDocumento.LE
                || tipoDocumento == TipoDocumento.LC) {
            return upper.replaceAll("[.\\s]+", "");
        }
        return upper;
    }

    /**
     * Parsea y valida completamente un archivo CSV en memoria.
     *
     * @param file archivo recibido por HTTP multipart
     * @return lista de filas validadas y tipadas
     * @throws CsvImportacionException si se detecta cualquier error estructural o de fila
     */
    public List<FilaCandidatoCsvDTO> parsear(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CsvImportacionException(
                    "El archivo CSV está vacío.",
                    List.of(new DetalleErrorFilaDTO(null, "file", "El archivo CSV está vacío."))
            );
        }

        String nombreArchivo = file.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new CsvImportacionException(
                    "El archivo debe tener extensión .csv.",
                    List.of(new DetalleErrorFilaDTO(null, "file", "El archivo seleccionado no tiene formato .csv válido."))
            );
        }

        try (InputStream is = file.getInputStream()) {
            return parsearInputStream(is);
        } catch (IOException e) {
            throw new CsvImportacionException(
                    "Error al leer el archivo CSV: " + e.getMessage(),
                    List.of(new DetalleErrorFilaDTO(null, "file", "No se pudo leer el contenido del archivo."))
            );
        }
    }

    /**
     * Parsea el flujo de entrada del CSV en memoria.
     *
     * @param inputStream flujo de bytes del archivo CSV
     * @return lista de filas tipadas
     */
    public List<FilaCandidatoCsvDTO> parsearInputStream(InputStream inputStream) {
        List<DetalleErrorFilaDTO> errores = new ArrayList<>();
        List<FilaCandidatoCsvDTO> filasValidas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String primeraLinea = reader.readLine();
            if (primeraLinea == null || primeraLinea.isBlank()) {
                throw new CsvImportacionException(
                        "El archivo CSV está vacío.",
                        List.of(new DetalleErrorFilaDTO(1, "encabezado", "El archivo está vacío o no contiene encabezado."))
                );
            }

            // Sanitizar posible BOM \uFEFF al inicio del archivo
            if (primeraLinea.startsWith("\uFEFF")) {
                primeraLinea = primeraLinea.substring(1);
            }

            // Validar separador punto y coma (;)
            if (!primeraLinea.contains(SEPARADOR)) {
                throw new CsvImportacionException(
                        "El archivo no utiliza el separador requerido.",
                        List.of(new DetalleErrorFilaDTO(1, "encabezado", "El archivo debe utilizar ';' (punto y coma) como separador de columnas."))
                );
            }

            Map<String, Integer> indiceColumnas = mapearEncabezados(primeraLinea, errores);
            if (!errores.isEmpty()) {
                throw new CsvImportacionException("El encabezado del archivo CSV es inválido.", errores);
            }

            Map<String, Integer> documentosVistos = new HashMap<>();
            int nroFila = 2;
            String linea;

            while ((linea = reader.readLine()) != null) {
                if (linea.isBlank()) {
                    nroFila++;
                    continue;
                }

                procesarFila(linea, nroFila, indiceColumnas, documentosVistos, errores, filasValidas);
                nroFila++;
            }

            if (filasValidas.isEmpty() && errores.isEmpty()) {
                throw new CsvImportacionException(
                        "El archivo CSV no contiene registros de candidatos.",
                        List.of(new DetalleErrorFilaDTO(null, "archivo", "El archivo solo posee encabezados sin filas de datos."))
                );
            }

        } catch (IOException e) {
            throw new CsvImportacionException(
                    "Error de lectura del archivo CSV.",
                    List.of(new DetalleErrorFilaDTO(null, "file", e.getMessage()))
            );
        }

        if (!errores.isEmpty()) {
            throw new CsvImportacionException("El archivo CSV contiene errores de validación.", errores);
        }

        return filasValidas;
    }

    private Map<String, Integer> mapearEncabezados(String encabezadoLinea, List<DetalleErrorFilaDTO> errores) {
        String[] headers = encabezadoLinea.split(SEPARADOR, -1);
        Map<String, Integer> indiceColumnas = new HashMap<>();

        for (int i = 0; i < headers.length; i++) {
            String canonico = normalizarNombreColumna(headers[i]);
            if (canonico != null && !canonico.isBlank()) {
                indiceColumnas.put(canonico, i);
            }
        }

        if (indiceColumnas.containsKey(COL_NRO_CANDIDATO)) {
            errores.add(new DetalleErrorFilaDTO(
                    1,
                    "Nro candidato",
                    "La columna 'Nro candidato' no debe incluirse en el archivo CSV; el número de candidato es asignado automáticamente por el sistema."
            ));
        }

        for (String requerida : COLUMNAS_REQUERIDAS) {
            if (!indiceColumnas.containsKey(requerida)) {
                errores.add(new DetalleErrorFilaDTO(
                        1,
                        requerida,
                        "Falta la columna requerida en el encabezado: '" + obtenerNombreLegibleColumna(requerida) + "'."
                ));
            }
        }

        return indiceColumnas;
    }

    private void procesarFila(
            String linea,
            int nroFila,
            Map<String, Integer> indiceColumnas,
            Map<String, Integer> documentosVistos,
            List<DetalleErrorFilaDTO> errores,
            List<FilaCandidatoCsvDTO> filasValidas
    ) {
        String[] celdas = linea.split(SEPARADOR, -1);

        String apellido = obtenerValor(celdas, indiceColumnas.get(COL_APELLIDO));
        String nombre = obtenerValor(celdas, indiceColumnas.get(COL_NOMBRE));
        String nroDocumentoCrudo = obtenerValor(celdas, indiceColumnas.get(COL_NRO_DOCUMENTO));
        String tipoDocStr = obtenerValor(celdas, indiceColumnas.get(COL_TIPO_DOCUMENTO));
        String fechaNacStr = obtenerValor(celdas, indiceColumnas.get(COL_FECHA_NACIMIENTO));
        String generoStr = obtenerValor(celdas, indiceColumnas.get(COL_GENERO));
        String nacionalidad = obtenerValor(celdas, indiceColumnas.get(COL_NACIONALIDAD));
        String email = obtenerValor(celdas, indiceColumnas.get(COL_EMAIL));
        String escolaridad = obtenerValor(celdas, indiceColumnas.get(COL_ESCOLARIDAD));
        String eliminadoStr = obtenerValor(celdas, indiceColumnas.get(COL_ELIMINADO));

        boolean filaConError = false;

        // 1. Apellido
        if (apellido.isBlank()) {
            errores.add(new DetalleErrorFilaDTO(nroFila, "Apellido", "El apellido es obligatorio y no puede estar vacío."));
            filaConError = true;
        }

        // 2. Nombre
        if (nombre.isBlank()) {
            errores.add(new DetalleErrorFilaDTO(nroFila, "Nombre", "El nombre es obligatorio y no puede estar vacío."));
            filaConError = true;
        }

        // 3. Tipo documento
        TipoDocumento tipoDocumento = null;
        if (tipoDocStr.isBlank()) {
            errores.add(new DetalleErrorFilaDTO(nroFila, "Tipo documento", "El tipo de documento es obligatorio."));
            filaConError = true;
        } else {
            try {
                tipoDocumento = TipoDocumento.valueOf(tipoDocStr.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                errores.add(new DetalleErrorFilaDTO(
                        nroFila,
                        "Tipo documento",
                        "Tipo de documento inválido: '" + tipoDocStr + "'. Valores permitidos: DNI, LE, LC, PP."
                ));
                filaConError = true;
            }
        }

        // 4. Nro documento (normalizado según tipo + control de duplicados intra-archivo por Tipo + Nro)
        String nroDocumentoNormalizado = normalizarNroDocumento(tipoDocumento, nroDocumentoCrudo);
        boolean nroDocumentoValido = false;
        if (nroDocumentoNormalizado.isBlank()) {
            errores.add(new DetalleErrorFilaDTO(nroFila, "Nro documento", "El número de documento es obligatorio y no puede estar vacío."));
            filaConError = true;
        } else if ((tipoDocumento == TipoDocumento.DNI
                || tipoDocumento == TipoDocumento.LE
                || tipoDocumento == TipoDocumento.LC)
                && !SOLO_DIGITOS_PATTERN.matcher(nroDocumentoNormalizado).matches()) {
            errores.add(new DetalleErrorFilaDTO(
                    nroFila,
                    "Nro documento",
                    "El número de documento '" + nroDocumentoCrudo + "' para " + tipoDocumento.name()
                            + " debe contener solo dígitos (los puntos y espacios se quitan automáticamente)."
            ));
            filaConError = true;
        } else {
            nroDocumentoValido = true;
        }

        if (tipoDocumento != null && nroDocumentoValido) {
            String claveDocumento = tipoDocumento.name() + "::" + nroDocumentoNormalizado;
            if (documentosVistos.containsKey(claveDocumento)) {
                int filaPrevia = documentosVistos.get(claveDocumento);
                errores.add(new DetalleErrorFilaDTO(
                        nroFila,
                        "Nro documento",
                        "El documento " + tipoDocumento.name() + " '" + nroDocumentoNormalizado
                                + "' está duplicado dentro del archivo (ya figura en fila " + filaPrevia + ")."
                ));
                filaConError = true;
            } else {
                documentosVistos.put(claveDocumento, nroFila);
            }
        }

        // 5. Fecha de nacimiento (yyyy-MM-dd estricto)
        LocalDate fechaNacimiento = null;
        if (fechaNacStr.isBlank()) {
            errores.add(new DetalleErrorFilaDTO(nroFila, "Fecha de nacimiento", "La fecha de nacimiento es obligatoria."));
            filaConError = true;
        } else {
            try {
                fechaNacimiento = LocalDate.parse(fechaNacStr.trim(), FECHA_FORMATTER);
            } catch (DateTimeParseException e) {
                errores.add(new DetalleErrorFilaDTO(
                        nroFila,
                        "Fecha de nacimiento",
                        "Fecha de nacimiento inválida: '" + fechaNacStr + "'. Formato estricto requerido: aaaa-mm-dd (yyyy-MM-dd)."
                ));
                filaConError = true;
            }
        }

        // 6. Género
        Genero genero = null;
        if (generoStr.isBlank()) {
            errores.add(new DetalleErrorFilaDTO(nroFila, "Género", "El género es obligatorio."));
            filaConError = true;
        } else {
            try {
                genero = Genero.valueOf(generoStr.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                errores.add(new DetalleErrorFilaDTO(
                        nroFila,
                        "Género",
                        "Género inválido: '" + generoStr + "'. Valores permitidos: H, M."
                ));
                filaConError = true;
            }
        }

        // 7. Nacionalidad
        String nacionalidadCanonica = null;
        if (nacionalidad.isBlank()) {
            errores.add(new DetalleErrorFilaDTO(nroFila, "Nacionalidad", "La nacionalidad es obligatoria y no puede estar vacía."));
            filaConError = true;
        } else {
            Optional<Nacionalidad> nacOpt = Nacionalidad.parse(nacionalidad);
            if (nacOpt.isEmpty()) {
                errores.add(new DetalleErrorFilaDTO(
                        nroFila,
                        "Nacionalidad",
                        "Nacionalidad inválida: '" + nacionalidad + "'. Se admiten países o gentilicios de Latinoamérica (ej: Argentina, Argentino/a, Brasil, Chile, Uruguay, etc.)."
                ));
                filaConError = true;
            } else {
                nacionalidadCanonica = nacOpt.get().name();
            }
        }

        // 8. Email
        if (email.isBlank()) {
            errores.add(new DetalleErrorFilaDTO(nroFila, "Email", "El email es obligatorio y no puede estar vacío."));
            filaConError = true;
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errores.add(new DetalleErrorFilaDTO(
                    nroFila,
                    "Email",
                    "Formato de email inválido: '" + email + "'."
            ));
            filaConError = true;
        }

        // 9. Escolaridad
        String escolaridadCanonica = null;
        if (escolaridad.isBlank()) {
            errores.add(new DetalleErrorFilaDTO(nroFila, "Escolaridad", "La escolaridad es obligatoria y no puede estar vacía."));
            filaConError = true;
        } else {
            Optional<Escolaridad> escOpt = Escolaridad.parse(escolaridad);
            if (escOpt.isEmpty()) {
                errores.add(new DetalleErrorFilaDTO(
                        nroFila,
                        "Escolaridad",
                        "Nivel de escolaridad inválido: '" + escolaridad + "'. Valores permitidos: Primario, Secundario, Terciario, Universitario (y sus variantes Completo / Incompleto)."
                ));
                filaConError = true;
            } else {
                escolaridadCanonica = escOpt.get().getDescripcion();
            }
        }

        // 10. Eliminado (True / False case-insensitive)
        Boolean eliminado = null;
        if (eliminadoStr.isBlank()) {
            errores.add(new DetalleErrorFilaDTO(nroFila, "Eliminado", "El campo Eliminado es obligatorio."));
            filaConError = true;
        } else {
            String elimNormalizado = eliminadoStr.trim();
            if ("true".equalsIgnoreCase(elimNormalizado)) {
                eliminado = Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(elimNormalizado)) {
                eliminado = Boolean.FALSE;
            } else {
                errores.add(new DetalleErrorFilaDTO(
                        nroFila,
                        "Eliminado",
                        "Valor inválido para Eliminado: '" + eliminadoStr + "'. Valores permitidos: True o False."
                ));
                filaConError = true;
            }
        }

        if (!filaConError) {
            filasValidas.add(new FilaCandidatoCsvDTO(
                    nroFila,
                    apellido.trim().toUpperCase(Locale.ROOT),
                    nombre.trim().toUpperCase(Locale.ROOT),
                    nroDocumentoNormalizado,
                    tipoDocumento,
                    fechaNacimiento,
                    genero,
                    nacionalidadCanonica,
                    email.trim().toLowerCase(Locale.ROOT),
                    escolaridadCanonica,
                    eliminado
            ));
        }
    }

    private String obtenerValor(String[] celdas, Integer indice) {
        if (indice == null || indice < 0 || indice >= celdas.length) {
            return "";
        }
        return celdas[indice] != null ? celdas[indice].trim() : "";
    }

    private String normalizarNombreColumna(String header) {
        if (header == null) {
            return "";
        }
        String limpio = header.replace("\uFEFF", "").trim();
        // Quitar tildes/acentos
        limpio = Normalizer.normalize(limpio, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);

        // Remover puntos, guiones y espacios múltiples
        limpio = limpio.replace(".", "").replace("-", " ").replaceAll("\\s+", " ").trim();

        return switch (limpio) {
            case "NRO CANDIDATO", "NUMERO CANDIDATO", "NUMERO DE CANDIDATO", "NRO DE CANDIDATO" -> COL_NRO_CANDIDATO;
            case "APELLIDO", "APELLIDOS" -> COL_APELLIDO;
            case "NOMBRE", "NOMBRES" -> COL_NOMBRE;
            case "NRO DOCUMENTO", "NUMERO DOCUMENTO", "NUMERO DE DOCUMENTO", "NRO DE DOCUMENTO" -> COL_NRO_DOCUMENTO;
            case "TIPO DOCUMENTO", "TIPO DE DOCUMENTO" -> COL_TIPO_DOCUMENTO;
            case "FECHA DE NACIMIENTO", "FECHA NACIMIENTO" -> COL_FECHA_NACIMIENTO;
            case "GENERO", "SEXO" -> COL_GENERO;
            case "NACIONALIDAD" -> COL_NACIONALIDAD;
            case "EMAIL", "CORREO", "CORREO ELECTRONICO" -> COL_EMAIL;
            case "ESCOLARIDAD", "NIVEL DE ESTUDIOS" -> COL_ESCOLARIDAD;
            case "ELIMINADO", "BAJA" -> COL_ELIMINADO;
            default -> limpio.replace(" ", "_");
        };
    }

    private String obtenerNombreLegibleColumna(String codigoColumna) {
        return switch (codigoColumna) {
            case COL_NRO_CANDIDATO -> "Nro candidato";
            case COL_APELLIDO -> "Apellido";
            case COL_NOMBRE -> "Nombre";
            case COL_NRO_DOCUMENTO -> "Nro documento";
            case COL_TIPO_DOCUMENTO -> "Tipo documento";
            case COL_FECHA_NACIMIENTO -> "Fecha de nacimiento";
            case COL_GENERO -> "Género";
            case COL_NACIONALIDAD -> "Nacionalidad";
            case COL_EMAIL -> "Email";
            case COL_ESCOLARIDAD -> "Escolaridad";
            case COL_ELIMINADO -> "Eliminado";
            default -> codigoColumna;
        };
    }
}
