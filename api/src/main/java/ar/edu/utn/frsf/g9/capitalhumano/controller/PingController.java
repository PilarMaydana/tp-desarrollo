package ar.edu.utn.frsf.g9.capitalhumano.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador de verificación de conectividad.
 *
 * <p>Expone un endpoint simple para que el frontend pueda comprobar
 * que la API está levantada y respondiendo.</p>
 */
@RestController
@RequestMapping("/api")
public class PingController {

    /**
     * Endpoint de health-check básico.
     *
     * @return {@code {"status":"ok"}} con HTTP 200
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
