package ar.edu.utn.frsf.g9.capitalhumano.config;

import ar.edu.utn.frsf.g9.capitalhumano.shared.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad HTTP.
 *
 * <p>Registra {@link JwtAuthenticationFilter} para autenticar peticiones con header
 * {@code Authorization: Bearer <token>} y poblar el {@code SecurityContextHolder}.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Cadena de filtros de seguridad HTTP con soporte de autenticación JWT.
     *
     * @param http builder de seguridad HTTP
     * @return cadena de filtros configurada
     * @throws Exception si falla la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // TODO CU 01: exigir autenticación
                        .requestMatchers("/api/competencias/**").permitAll()
                        .requestMatchers("/api/puestos/**").permitAll()
                        // TODO CU 01: exigir autenticación
                        .requestMatchers("/api/factores/**").permitAll()
                        // TODO CU 01: exigir autenticación
                        .requestMatchers("/api/preguntas/**").permitAll()
                        // TODO CU 01: exigir autenticación
                        .requestMatchers("/api/opciones-respuesta/**").permitAll()
                        // TODO CU 01: exigir autenticación y rol CONSULTOR
                        .requestMatchers("/api/candidatos/**").permitAll()
                        // TODO CU 01: exigir autenticación
                        .requestMatchers("/api/evaluaciones/**").permitAll()
                        // TODO CU 01: exigir autenticación
                        .requestMatchers("/api/cuestionarios/**").permitAll()
                        // TODO CU 01: exigir autenticación
                        .requestMatchers("/api/orden-merito/**").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
