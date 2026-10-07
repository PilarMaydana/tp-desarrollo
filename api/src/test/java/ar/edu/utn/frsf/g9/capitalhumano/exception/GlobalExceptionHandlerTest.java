package ar.edu.utn.frsf.g9.capitalhumano.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests unitarios para {@link GlobalExceptionHandler} utilizando MockMvc en modo standaloneSetup.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("1. RecursoNoEncontradoException -> 404, codigo y path correctos")
    void testRecursoNoEncontrado() throws Exception {
        mockMvc.perform(get("/test/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensaje").value("Usuario con id 42 no existe"))
                .andExpect(jsonPath("$.path").value("/test/no-encontrado"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.detalles").isArray());
    }

    @Test
    @DisplayName("2. RecursoDuplicadoException -> 409")
    void testRecursoDuplicado() throws Exception {
        mockMvc.perform(get("/test/duplicado"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.codigo").value("RECURSO_DUPLICADO"))
                .andExpect(jsonPath("$.mensaje").value("El correo ya se encuentra registrado"))
                .andExpect(jsonPath("$.path").value("/test/duplicado"));
    }

    @Test
    @DisplayName("3. ReglaNegocioException -> 422")
    void testReglaNegocio() throws Exception {
        mockMvc.perform(get("/test/regla-negocio"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"))
                .andExpect(jsonPath("$.mensaje").value("No se puede eliminar una postulacion activa"))
                .andExpect(jsonPath("$.path").value("/test/regla-negocio"));
    }

    @Test
    @DisplayName("4. Body con @NotBlank vacio -> 400, codigo VALIDACION, detalles[0].campo correcto")
    void testValidacionCampos() throws Exception {
        mockMvc.perform(post("/test/validacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.path").value("/test/validacion"))
                .andExpect(jsonPath("$.detalles[0].campo").value("nombre"))
                .andExpect(jsonPath("$.detalles[0].mensaje").value("El nombre no puede estar vacio"));
    }

    @Test
    @DisplayName("5. JSON malformado -> 400, codigo CUERPO_INVALIDO")
    void testJsonMalformado() throws Exception {
        mockMvc.perform(post("/test/cuerpo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("CUERPO_INVALIDO"))
                .andExpect(jsonPath("$.path").value("/test/cuerpo"));
    }

    @Test
    @DisplayName("6. IllegalStateException -> 500, mensaje generico sin exponer detalle interno")
    void testErrorInterno() throws Exception {
        mockMvc.perform(get("/test/error-interno"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.mensaje").value("Ocurrio un error inesperado"))
                .andExpect(jsonPath("$.path").value("/test/error-interno"))
                .andExpect(content().string(not(containsString("detalle interno confidencial"))));
    }

    @Test
    @DisplayName("7. POST a un endpoint que solo acepta GET -> 405 (respaldo conserva status de Spring MVC)")
    void testMetodoNoSoportado() throws Exception {
        mockMvc.perform(post("/test/solo-get"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.codigo").value("ERROR_SOLICITUD"))
                .andExpect(jsonPath("$.path").value("/test/solo-get"))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/no-encontrado")
        public void recursoNoEncontrado() {
            throw new RecursoNoEncontradoException("Usuario", 42L);
        }

        @GetMapping("/duplicado")
        public void recursoDuplicado() {
            throw new RecursoDuplicadoException("El correo ya se encuentra registrado");
        }

        @GetMapping("/regla-negocio")
        public void reglaNegocio() {
            throw new ReglaNegocioException("No se puede eliminar una postulacion activa");
        }

        @PostMapping("/validacion")
        public void validacion(@Valid @RequestBody TestDto dto) {
            // No-op
        }

        @PostMapping("/cuerpo")
        public void cuerpoInvalido(@RequestBody TestDto dto) {
            // No-op
        }

        @GetMapping("/error-interno")
        public void errorInterno() {
            throw new IllegalStateException("detalle interno confidencial");
        }

        @GetMapping("/solo-get")
        public String soloGet() {
            return "ok";
        }
    }

    static class TestDto {

        @NotBlank(message = "El nombre no puede estar vacio")
        private String nombre;

        public TestDto() {
        }

        public TestDto(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
    }
}
