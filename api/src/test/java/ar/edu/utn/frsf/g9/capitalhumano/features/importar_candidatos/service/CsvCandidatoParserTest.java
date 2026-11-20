package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Genero;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.FilaCandidatoCsvDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.exception.CsvImportacionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvCandidatoParserTest {

    private CsvCandidatoParser parser;

    private static final String HEADER_VALIDO =
            "Apellido;Nombre;Nro documento;Tipo documento;Fecha de nacimiento;Género;Nacionalidad;Email;Escolaridad;Eliminado\n";

    @BeforeEach
    void setUp() {
        parser = new CsvCandidatoParser();
    }

    @Test
    @DisplayName("Debe parsear correctamente un archivo CSV válido con BOM, delimitador ';' y normalización de documentos")
    void parsear_csvValido_exito() {
        String csvContent = "\uFEFF" + HEADER_VALIDO
                + "PEREZ;JUAN;35.123.456;DNI;1990-05-14;H;ARGENTINA;juan.perez@mail.com;UNIVERSITARIO;False\n"
                + "GOMEZ;MARIA;ab 123456;PP;1995-11-22;M;ARGENTINA;maria.gomez@mail.com;TERCIARIO;True\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "candidatos.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        List<FilaCandidatoCsvDTO> resultado = parser.parsear(file);

        assertThat(resultado).hasSize(2);

        FilaCandidatoCsvDTO fila1 = resultado.get(0);
        assertThat(fila1.nroFila()).isEqualTo(2);
        assertThat(fila1.apellido()).isEqualTo("PEREZ");
        assertThat(fila1.nombre()).isEqualTo("JUAN");
        assertThat(fila1.nroDocumento()).isEqualTo("35123456");
        assertThat(fila1.tipoDocumento()).isEqualTo(TipoDocumento.DNI);
        assertThat(fila1.fechaNacimiento()).isEqualTo(LocalDate.of(1990, 5, 14));
        assertThat(fila1.genero()).isEqualTo(Genero.H);
        assertThat(fila1.nacionalidad()).isEqualTo("ARGENTINA");
        assertThat(fila1.email()).isEqualTo("juan.perez@mail.com");
        assertThat(fila1.escolaridad()).isEqualTo("UNIVERSITARIO");
        assertThat(fila1.eliminado()).isFalse();

        FilaCandidatoCsvDTO fila2 = resultado.get(1);
        assertThat(fila2.nroFila()).isEqualTo(3);
        assertThat(fila2.tipoDocumento()).isEqualTo(TipoDocumento.PP);
        assertThat(fila2.nroDocumento()).isEqualTo("AB123456");
        assertThat(fila2.eliminado()).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar con error 400 si el archivo incluye la columna 'Nro candidato'")
    void parsear_conColumnaNroCandidato_lanzaExcepcion() {
        String csvContent = "Nro candidato;" + HEADER_VALIDO
                + "1;PEREZ;JUAN;35123456;DNI;1990-05-14;H;ARGENTINA;juan.perez@mail.com;UNIVERSITARIO;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "con_nro.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> parser.parsear(file))
                .isInstanceOf(CsvImportacionException.class)
                .hasMessageContaining("El encabezado del archivo CSV es inválido.")
                .satisfies(ex -> {
                    CsvImportacionException cie = (CsvImportacionException) ex;
                    assertThat(cie.getErrores()).anyMatch(e ->
                            "Nro candidato".equals(e.campo())
                                    && e.fila() == 1
                                    && e.mensaje().contains("asignado automáticamente por el sistema")
                    );
                });
    }

    @Test
    @DisplayName("Debe lanzar excepción si el archivo está vacío")
    void parsear_archivoVacio_lanzaExcepcion() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "vacio.csv", "text/csv", new byte[0]
        );

        assertThatThrownBy(() -> parser.parsear(file))
                .isInstanceOf(CsvImportacionException.class)
                .hasMessageContaining("El archivo CSV está vacío.");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el archivo no tiene extensión .csv")
    void parsear_extensionInvalida_lanzaExcepcion() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "candidatos.xlsx", "application/vnd.ms-excel", "contenido".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> parser.parsear(file))
                .isInstanceOf(CsvImportacionException.class)
                .hasMessageContaining("El archivo debe tener extensión .csv.");
    }

    @Test
    @DisplayName("Debe lanzar excepción si se utilizan comas en lugar de punto y coma")
    void parsear_separadorComa_lanzaExcepcion() {
        String csvContent = "Apellido,Nombre,Nro documento,Tipo documento,Fecha de nacimiento,Género,Nacionalidad,Email,Escolaridad,Eliminado\n"
                + "PEREZ,JUAN,35123456,DNI,1990-05-14,H,ARGENTINA,juan.perez@mail.com,UNIVERSITARIO,False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "comas.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> parser.parsear(file))
                .isInstanceOf(CsvImportacionException.class)
                .hasMessageContaining("El archivo no utiliza el separador requerido.");
    }

    @Test
    @DisplayName("Debe lanzar excepción si faltan columnas requeridas en el encabezado")
    void parsear_encabezadoIncompleto_lanzaExcepcion() {
        String csvContent = "Apellido;Nombre;Nro documento\n"
                + "PEREZ;JUAN;35123456\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "incompleto.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> parser.parsear(file))
                .isInstanceOf(CsvImportacionException.class)
                .hasMessageContaining("El encabezado del archivo CSV es inválido.");
    }

    @Test
    @DisplayName("Debe reportar múltiples errores de fila (fecha inválida, enum fuera de rango, email inválido)")
    void parsear_filasInvalidas_reportaErrores() {
        String csvContent = HEADER_VALIDO
                + "PEREZ;JUAN;35123456;PASAPORTE_INVALIDO;1990-02-31;X;ARGENTINA;emailInvalido;UNIVERSITARIO;TalVez\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "errores.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> parser.parsear(file))
                .isInstanceOf(CsvImportacionException.class)
                .satisfies(ex -> {
                    CsvImportacionException cie = (CsvImportacionException) ex;
                    assertThat(cie.getErrores()).isNotEmpty();
                    assertThat(cie.getErrores()).anyMatch(e -> "Tipo documento".equals(e.campo()));
                    assertThat(cie.getErrores()).anyMatch(e -> "Fecha de nacimiento".equals(e.campo()));
                    assertThat(cie.getErrores()).anyMatch(e -> "Género".equals(e.campo()));
                    assertThat(cie.getErrores()).anyMatch(e -> "Email".equals(e.campo()));
                    assertThat(cie.getErrores()).anyMatch(e -> "Eliminado".equals(e.campo()));
                });
    }

    @Test
    @DisplayName("Debe rechazar el archivo si el mismo par (Tipo documento, Nro documento) se repite incluso con distinta puntuación")
    void parsear_documentosDuplicadosIntraArchivo_lanzaExcepcion() {
        String csvContent = HEADER_VALIDO
                + "PEREZ;JUAN;35.123.456;DNI;1990-05-14;H;ARGENTINA;juan.perez@mail.com;UNIVERSITARIO;False\n"
                + "PEREZ;JUAN CARLOS;35123456;DNI;1990-05-14;H;ARGENTINA;otro@mail.com;UNIVERSITARIO;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "duplicados.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> parser.parsear(file))
                .isInstanceOf(CsvImportacionException.class)
                .satisfies(ex -> {
                    CsvImportacionException cie = (CsvImportacionException) ex;
                    assertThat(cie.getErrores()).anyMatch(e ->
                            "Nro documento".equals(e.campo())
                                    && e.mensaje().contains("duplicado dentro del archivo")
                                    && e.fila() == 3
                    );
                });
    }

    @Test
    @DisplayName("Debe permitir el mismo número de documento si el Tipo documento es distinto")
    void parsear_mismoNumeroDistintoTipoDocumento_exito() {
        String csvContent = HEADER_VALIDO
                + "PEREZ;JUAN;35123456;DNI;1990-05-14;H;ARGENTINA;juan.perez@mail.com;UNIVERSITARIO;False\n"
                + "GOMEZ;ANA;35123456;PP;1992-08-10;M;ARGENTINA;ana.gomez@mail.com;TERCIARIO;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "distinto_tipo.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        List<FilaCandidatoCsvDTO> resultado = parser.parsear(file);
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).tipoDocumento()).isEqualTo(TipoDocumento.DNI);
        assertThat(resultado.get(1).tipoDocumento()).isEqualTo(TipoDocumento.PP);
    }

    @Test
    @DisplayName("Debe rechazar letras en Nro documento para tipos DNI, LE y LC")
    void parsear_dniConLetras_lanzaExcepcion() {
        String csvContent = HEADER_VALIDO
                + "PEREZ;JUAN;35A23456;DNI;1990-05-14;H;ARGENTINA;juan.perez@mail.com;UNIVERSITARIO;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "dni_letras.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> parser.parsear(file))
                .isInstanceOf(CsvImportacionException.class)
                .satisfies(ex -> {
                    CsvImportacionException cie = (CsvImportacionException) ex;
                    assertThat(cie.getErrores()).anyMatch(e ->
                            "Nro documento".equals(e.campo()) && e.mensaje().contains("solo dígitos")
                    );
                });
    }

    @Test
    @DisplayName("Debe parsear nacionalidades latinoamericanas (países y gentilicios masc/fem) y variantes de escolaridad")
    void parsear_nacionalidadesYEscolaridad_variantesValidas() {
        String csvContent = HEADER_VALIDO
                + "PEREZ;JUAN;35123456;DNI;1990-05-14;H;argentino;juan.perez@mail.com;universitario incompleto;False\n"
                + "SILVA;ANA;38987654;DNI;1995-11-22;M;brasileña;ana.silva@mail.com;Secundario Completo;False\n"
                + "ROJAS;LUIS;29456789;LE;1982-03-08;H;chileno;luis.rojas@mail.com;primario;False\n"
                + "GARCIA;MARIA;31112233;DNI;1988-07-19;M;MÉXICO;maria.garcia@mail.com;TERCIARIO INCOMPLETO;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "candidatos_latam.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        List<FilaCandidatoCsvDTO> resultado = parser.parsear(file);

        assertThat(resultado).hasSize(4);
        assertThat(resultado.get(0).nacionalidad()).isEqualTo("ARGENTINA");
        assertThat(resultado.get(0).escolaridad()).isEqualTo("UNIVERSITARIO INCOMPLETO");

        assertThat(resultado.get(1).nacionalidad()).isEqualTo("BRASIL");
        assertThat(resultado.get(1).escolaridad()).isEqualTo("SECUNDARIO COMPLETO");

        assertThat(resultado.get(2).nacionalidad()).isEqualTo("CHILE");
        assertThat(resultado.get(2).escolaridad()).isEqualTo("PRIMARIO");

        assertThat(resultado.get(3).nacionalidad()).isEqualTo("MEXICO");
        assertThat(resultado.get(3).escolaridad()).isEqualTo("TERCIARIO INCOMPLETO");
    }

    @Test
    @DisplayName("Debe rechazar nacionalidades no latinoamericanas o escolaridades no permitidas")
    void parsear_nacionalidadYEscolaridad_invalidas_reportaErrores() {
        String csvContent = HEADER_VALIDO
                + "MULLER;HANS;35123456;DNI;1990-05-14;H;ALEMANIA;hans@mail.com;DOCTORADO_FANTASMA;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "invalidos.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> parser.parsear(file))
                .isInstanceOf(CsvImportacionException.class)
                .satisfies(ex -> {
                    CsvImportacionException cie = (CsvImportacionException) ex;
                    assertThat(cie.getErrores()).anyMatch(e ->
                            "Nacionalidad".equals(e.campo()) && e.mensaje().contains("Nacionalidad inválida")
                    );
                    assertThat(cie.getErrores()).anyMatch(e ->
                            "Escolaridad".equals(e.campo()) && e.mensaje().contains("Nivel de escolaridad inválido")
                    );
                });
    }
}
