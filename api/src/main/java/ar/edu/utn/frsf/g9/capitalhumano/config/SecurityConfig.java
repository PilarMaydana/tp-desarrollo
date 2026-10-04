package ar.edu.utn.frsf.g9.capitalhumano.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad <b>temporal</b>.
 *
 * <p>Permite todas las peticiones sin autenticación y deshabilita CSRF
 * para facilitar el desarrollo inicial. Se necesita porque Spring Security
 * bloquea todo por defecto al estar en el classpath.</p>
 *
 * <p><b>TODO:</b> reemplazar por la configuración real con LDAP
 * al implementar el CU 01 (Autenticación).</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Cadena de filtros que deja pasar todas las peticiones.
     *
     * @param http builder de seguridad HTTP
     * @return cadena de filtros configurada
     * @throws Exception si falla la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // TODO: reemplazar por autenticación LDAP al implementar el CU 01
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
