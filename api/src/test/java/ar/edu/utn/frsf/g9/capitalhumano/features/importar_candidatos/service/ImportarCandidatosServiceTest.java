package ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Genero;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dao.CandidatoImportRepository;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.dto.ImportacionCandidatosResponseDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.importar_candidatos.exception.CsvImportacionException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.ContextoSeguridad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportarCandidatosServiceTest {

    @Mock
    private CandidatoImportRepository candidatoRepository;

    @Mock
    private ContextoSeguridad contextoSeguridad;

    @Captor
    private ArgumentCaptor<List<Candidato>> candidatosCaptor;

    private CsvCandidatoParser csvCandidatoParser;
    private ImportarCandidatosService service;

    private static final Long USUARIO_CONSULTOR_ID = 42L;
    private static final String HEADER_VALIDO =
            "Apellido;Nombre;Nro documento;Tipo documento;Fecha de nacimiento;Género;Nacionalidad;Email;Escolaridad;Eliminado\n";

    @BeforeEach
    void setUp() {
        csvCandidatoParser = new CsvCandidatoParser();
        service = new ImportarCandidatosService(candidatoRepository, csvCandidatoParser, contextoSeguridad);
    }

    @Test
    @DisplayName("Importación inicial en BD vacía: Crea 2 activos y 1 eliminado asignando nro_candidato 1, 2 y 3")
    void importarCandidatos_importacionInicial_asignaCorrelativosDesdeUno() {
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(USUARIO_CONSULTOR_ID);
        when(candidatoRepository.findAllByNroDocumentoIn(anyCollection())).thenReturn(Collections.emptyList());
        when(candidatoRepository.findAllNrosCandidato()).thenReturn(Collections.emptyList());

        String csvContent = HEADER_VALIDO
                + "PEREZ;JUAN;35123456;DNI;1990-05-14;H;ARGENTINA;juan.perez@mail.com;UNIVERSITARIO;False\n"
                + "GOMEZ;MARIA;38987654;DNI;1995-11-22;M;ARGENTINA;maria.gomez@mail.com;TERCIARIO;False\n"
                + "LOPEZ;CARLOS;29456789;LE;1982-03-08;H;ARGENTINA;carlos.lopez@mail.com;UNIVERSITARIO;True\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "candidatos_ejemplo.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportacionCandidatosResponseDTO respuesta = service.importarCandidatos(file);

        assertThat(respuesta.candidatosCreados()).isEqualTo(2);
        assertThat(respuesta.candidatosActualizados()).isEqualTo(0);
        assertThat(respuesta.candidatosEliminados()).isEqualTo(1);

        verify(candidatoRepository).saveAll(candidatosCaptor.capture());
        List<Candidato> guardados = candidatosCaptor.getValue();
        assertThat(guardados).hasSize(3);

        assertThat(guardados).extracting(Candidato::getNroCandidato)
                .containsExactly("1", "2", "3");

        Candidato cand1 = guardados.get(0);
        assertThat(cand1.isEliminado()).isFalse();
        assertThat(cand1.getUsuarioEliminacion()).isNull();

        Candidato cand3 = guardados.get(2);
        assertThat(cand3.isEliminado()).isTrue();
        assertThat(cand3.getFechaEliminacion()).isNotNull();
        assertThat(cand3.getUsuarioEliminacion()).isEqualTo(USUARIO_CONSULTOR_ID);
    }

    @Test
    @DisplayName("Importación sobre BD existente: Asigna MAX + 1 ignorando valores legacy no numéricos")
    void importarCandidatos_conRegistrosPrevios_asignaMaxMasUno() {
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(USUARIO_CONSULTOR_ID);
        when(candidatoRepository.findAllByNroDocumentoIn(anyCollection())).thenReturn(Collections.emptyList());
        when(candidatoRepository.findAllNrosCandidato()).thenReturn(List.of("1", "7", "CAND-999", "005"));

        String csvContent = HEADER_VALIDO
                + "NUEVO;UNO;41000001;DNI;1997-01-10;H;ARGENTINA;uno@mail.com;UNIVERSITARIO;False\n"
                + "NUEVO;DOS;41000002;DNI;1998-02-20;M;ARGENTINA;dos@mail.com;TERCIARIO;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "nuevos.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportacionCandidatosResponseDTO respuesta = service.importarCandidatos(file);

        assertThat(respuesta.candidatosCreados()).isEqualTo(2);
        verify(candidatoRepository).saveAll(candidatosCaptor.capture());
        assertThat(candidatosCaptor.getValue())
                .extracting(Candidato::getNroCandidato)
                .containsExactly("8", "9");
    }

    @Test
    @DisplayName("Re-importación por (Tipo, Nro documento): Actualiza y da de baja conservando nro_candidato original")
    void importarCandidatos_reimportacionActualizacionYBaja_conservaNroCandidato() {
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(USUARIO_CONSULTOR_ID);

        Candidato existente1 = new Candidato();
        existente1.setId(1L);
        existente1.setNroCandidato("1");
        existente1.setApellido("PEREZ VIEJO");
        existente1.setNombre("JUAN");
        existente1.setNroDocumento("35123456");
        existente1.setTipoDocumento(TipoDocumento.DNI);
        existente1.setFechaNacimiento(LocalDate.of(1990, 5, 14));
        existente1.setGenero(Genero.H);
        existente1.setNacionalidad("ARGENTINA");
        existente1.setEmail("viejo@mail.com");
        existente1.setEscolaridad("SECUNDARIO");
        existente1.setEliminado(false);

        Candidato existente2 = new Candidato();
        existente2.setId(2L);
        existente2.setNroCandidato("2");
        existente2.setApellido("GOMEZ");
        existente2.setNombre("MARIA");
        existente2.setNroDocumento("38987654");
        existente2.setTipoDocumento(TipoDocumento.DNI);
        existente2.setFechaNacimiento(LocalDate.of(1995, 11, 22));
        existente2.setGenero(Genero.M);
        existente2.setNacionalidad("ARGENTINA");
        existente2.setEmail("maria.gomez@mail.com");
        existente2.setEscolaridad("TERCIARIO");
        existente2.setEliminado(false);

        when(candidatoRepository.findAllByNroDocumentoIn(anyCollection())).thenReturn(List.of(existente1, existente2));
        when(candidatoRepository.findAllNrosCandidato()).thenReturn(List.of("1", "2"));

        String csvContent = HEADER_VALIDO
                + "PEREZ NUEVO;JUAN;35.123.456;DNI;1990-05-14;H;ARGENTINA;nuevo@mail.com;UNIVERSITARIO;False\n"
                + "GOMEZ;MARIA;38987654;DNI;1995-11-22;M;ARGENTINA;maria.gomez@mail.com;TERCIARIO;True\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "actualizar.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportacionCandidatosResponseDTO respuesta = service.importarCandidatos(file);

        assertThat(respuesta.candidatosCreados()).isEqualTo(0);
        assertThat(respuesta.candidatosActualizados()).isEqualTo(1);
        assertThat(respuesta.candidatosEliminados()).isEqualTo(1);

        verify(candidatoRepository).saveAll(candidatosCaptor.capture());
        List<Candidato> guardados = candidatosCaptor.getValue();

        Candidato cand1 = guardados.stream().filter(c -> "35123456".equals(c.getNroDocumento())).findFirst().orElseThrow();
        assertThat(cand1.getNroCandidato()).isEqualTo("1");
        assertThat(cand1.getApellido()).isEqualTo("PEREZ NUEVO");
        assertThat(cand1.getEmail()).isEqualTo("nuevo@mail.com");
        assertThat(cand1.isEliminado()).isFalse();

        Candidato cand2 = guardados.stream().filter(c -> "38987654".equals(c.getNroDocumento())).findFirst().orElseThrow();
        assertThat(cand2.getNroCandidato()).isEqualTo("2");
        assertThat(cand2.isEliminado()).isTrue();
        assertThat(cand2.getFechaEliminacion()).isNotNull();
        assertThat(cand2.getUsuarioEliminacion()).isEqualTo(USUARIO_CONSULTOR_ID);
    }

    @Test
    @DisplayName("Re-importación con reactivación y mantenimiento de eliminado")
    void importarCandidatos_reactivacionYMantenimientoEliminado() {
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(USUARIO_CONSULTOR_ID);

        Candidato candPrevEliminado = new Candidato();
        candPrevEliminado.setId(10L);
        candPrevEliminado.setNroCandidato("10");
        candPrevEliminado.setTipoDocumento(TipoDocumento.DNI);
        candPrevEliminado.setNroDocumento("33445566");
        candPrevEliminado.setEliminado(true);
        candPrevEliminado.setFechaEliminacion(LocalDateTime.now().minusDays(5));
        candPrevEliminado.setUsuarioEliminacion(999L);

        LocalDateTime fechaElimOriginal = LocalDateTime.now().minusDays(10);
        Candidato candMantenerEliminado = new Candidato();
        candMantenerEliminado.setId(20L);
        candMantenerEliminado.setNroCandidato("20");
        candMantenerEliminado.setTipoDocumento(TipoDocumento.DNI);
        candMantenerEliminado.setNroDocumento("44556677");
        candMantenerEliminado.setEliminado(true);
        candMantenerEliminado.setFechaEliminacion(fechaElimOriginal);
        candMantenerEliminado.setUsuarioEliminacion(777L);

        when(candidatoRepository.findAllByNroDocumentoIn(anyCollection()))
                .thenReturn(List.of(candPrevEliminado, candMantenerEliminado));
        when(candidatoRepository.findAllNrosCandidato()).thenReturn(List.of("10", "20"));

        String csvContent = HEADER_VALIDO
                + "REACTIVADO;PEDRO;33445566;DNI;1991-01-01;H;ARGENTINA;pedro@mail.com;UNIVERSITARIO;False\n"
                + "CONTINUA;BAJA;44556677;DNI;1992-02-02;M;ARGENTINA;baja@mail.com;SECUNDARIO;True\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "reactivar.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportacionCandidatosResponseDTO respuesta = service.importarCandidatos(file);

        assertThat(respuesta.candidatosCreados()).isEqualTo(0);
        assertThat(respuesta.candidatosActualizados()).isEqualTo(1);
        assertThat(respuesta.candidatosEliminados()).isEqualTo(0);

        assertThat(candPrevEliminado.getNroCandidato()).isEqualTo("10");
        assertThat(candPrevEliminado.isEliminado()).isFalse();
        assertThat(candPrevEliminado.getFechaEliminacion()).isNull();
        assertThat(candPrevEliminado.getUsuarioEliminacion()).isNull();

        assertThat(candMantenerEliminado.getNroCandidato()).isEqualTo("20");
        assertThat(candMantenerEliminado.isEliminado()).isTrue();
        assertThat(candMantenerEliminado.getFechaEliminacion()).isEqualTo(fechaElimOriginal);
        assertThat(candMantenerEliminado.getUsuarioEliminacion()).isEqualTo(777L);
    }

    @Test
    @DisplayName("Debe rechazar con CsvImportacionException si el CSV incluye la columna Nro candidato sin tocar repositorio")
    void importarCandidatos_conColumnaNroCandidato_lanzaExcepcion() {
        String csvContent = "Nro candidato;" + HEADER_VALIDO
                + "1;PEREZ;JUAN;35123456;DNI;1990-05-14;H;ARGENTINA;juan.perez@mail.com;UNIVERSITARIO;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "con_nro.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> service.importarCandidatos(file))
                .isInstanceOf(CsvImportacionException.class)
                .hasMessageContaining("El encabezado del archivo CSV es inválido.");

        verifyNoInteractions(candidatoRepository);
    }

    @Test
    @DisplayName("Debe rechazar con CsvImportacionException ante separador con comas sin tocar repositorio")
    void importarCandidatos_separadorComa_lanzaExcepcion() {
        String csvContent = "Apellido,Nombre,Nro documento,Tipo documento,Fecha de nacimiento,Género,Nacionalidad,Email,Escolaridad,Eliminado\n"
                + "PEREZ,JUAN,35123456,DNI,1990-05-14,H,ARGENTINA,juan.perez@mail.com,UNIVERSITARIO,False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "comas.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> service.importarCandidatos(file))
                .isInstanceOf(CsvImportacionException.class)
                .hasMessageContaining("El archivo no utiliza el separador requerido.");

        verifyNoInteractions(candidatoRepository);
    }

    @Test
    @DisplayName("Debe rechazar con CsvImportacionException ante fecha inválida sin tocar repositorio")
    void importarCandidatos_fechaInvalida_lanzaExcepcion() {
        String csvContent = HEADER_VALIDO
                + "PEREZ;JUAN;35123456;DNI;fecha-mala;H;ARGENTINA;juan.perez@mail.com;UNIVERSITARIO;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "fecha_invalida.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> service.importarCandidatos(file))
                .isInstanceOf(CsvImportacionException.class)
                .satisfies(ex -> {
                    CsvImportacionException cie = (CsvImportacionException) ex;
                    assertThat(cie.getErrores()).anyMatch(e -> "Fecha de nacimiento".equals(e.campo()));
                });

        verifyNoInteractions(candidatoRepository);
    }

    @Test
    @DisplayName("Debe rechazar con CsvImportacionException ante TipoDocumento o Género fuera de enum")
    void importarCandidatos_enumsInvalidos_lanzaExcepcion() {
        String csvContent = HEADER_VALIDO
                + "PEREZ;JUAN;35123456;LIBRETA_CIVICA_MAL;1990-05-14;OTRO;ARGENTINA;juan.perez@mail.com;UNIVERSITARIO;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "enums_mal.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> service.importarCandidatos(file))
                .isInstanceOf(CsvImportacionException.class)
                .satisfies(ex -> {
                    CsvImportacionException cie = (CsvImportacionException) ex;
                    assertThat(cie.getErrores()).anyMatch(e -> "Tipo documento".equals(e.campo()));
                    assertThat(cie.getErrores()).anyMatch(e -> "Género".equals(e.campo()));
                });

        verifyNoInteractions(candidatoRepository);
    }

    @Test
    @DisplayName("Debe rechazar con CsvImportacionException ante (Tipo, Nro documento) duplicado en el mismo CSV")
    void importarCandidatos_duplicadosEnArchivo_lanzaExcepcion() {
        String csvContent = HEADER_VALIDO
                + "PEREZ;JUAN;35.123.456;DNI;1990-05-14;H;ARGENTINA;juan.perez@mail.com;UNIVERSITARIO;False\n"
                + "PEREZ;OTRO;35123456;DNI;1990-05-14;H;ARGENTINA;otro@mail.com;UNIVERSITARIO;False\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "duplicados.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> service.importarCandidatos(file))
                .isInstanceOf(CsvImportacionException.class)
                .satisfies(ex -> {
                    CsvImportacionException cie = (CsvImportacionException) ex;
                    assertThat(cie.getErrores()).anyMatch(e -> "Nro documento".equals(e.campo()) && e.fila() == 3);
                });

        verifyNoInteractions(candidatoRepository);
    }
}
