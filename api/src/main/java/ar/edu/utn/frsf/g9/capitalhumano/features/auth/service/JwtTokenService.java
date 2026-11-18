package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para la generación y firma de tokens JWT (JSON Web Tokens) bajo algoritmo HMAC-SHA256.
 */
@Service
public class JwtTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final String secretKey;
    private final long expirationSeconds;
    private final ObjectMapper objectMapper;

    public JwtTokenService(
            @Value("${app.jwt.secret:capital-humano-super-secret-key-for-jwt-signing-2026-utn-frsf}") String secretKey,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds,
            ObjectMapper objectMapper
    ) {
        this.secretKey = secretKey;
        this.expirationSeconds = expirationSeconds;
        this.objectMapper = objectMapper;
    }

    /**
     * Genera un token JWT para un usuario consultor.
     *
     * @param usuario nombre de usuario
     * @param usuarioId identificador numérico
     * @return token JWT firmado
     */
    public String generarTokenConsultor(String usuario, Long usuarioId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", usuario);
        claims.put("rol", "CONSULTOR");
        claims.put("usuarioId", usuarioId);
        return generarToken(claims);
    }

    /**
     * Genera un token JWT para un candidato que realiza un cuestionario.
     *
     * @param nroDocumento número de documento
     * @param candidatoId identificador del candidato
     * @param cuestionarioId identificador del cuestionario
     * @return token JWT firmado
     */
    public String generarTokenCandidato(String nroDocumento, Long candidatoId, Long cuestionarioId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", nroDocumento);
        claims.put("rol", "CANDIDATO");
        claims.put("usuarioId", candidatoId);
        claims.put("cuestionarioId", cuestionarioId);
        return generarToken(claims);
    }

    /**
     * Construye y firma un JWT a partir del mapa de claims.
     *
     * @param claims contenido a incluir en el payload
     * @return JWT firmado en formato standard (header.payload.signature)
     */
    public String generarToken(Map<String, Object> claims) {
        try {
            long now = Instant.now().getEpochSecond();
            Map<String, Object> payload = new HashMap<>(claims);
            payload.put("iat", now);
            payload.put("exp", now + expirationSeconds);

            Map<String, Object> header = Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            );

            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

            String headerEncoded = encoder.encodeToString(
                    objectMapper.writeValueAsString(header).getBytes(StandardCharsets.UTF_8)
            );
            String payloadEncoded = encoder.encodeToString(
                    objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8)
            );

            String contentToSign = headerEncoded + "." + payloadEncoded;
            String signatureEncoded = firmarHmacSha256(contentToSign, secretKey);

            return contentToSign + "." + signatureEncoded;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error al serializar claims de JWT", e);
        }
    }

    private String firmarHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Error al firmar token JWT con algoritmo HMAC-SHA256", e);
        }
    }
}
