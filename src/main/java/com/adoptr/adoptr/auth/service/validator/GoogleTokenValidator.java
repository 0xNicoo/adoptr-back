package com.adoptr.adoptr.auth.service.validator;

import com.adoptr.adoptr.auth.util.AuthUser;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GoogleTokenValidator implements Validator {
    private static final String GOOGLE_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";

    private final DefaultResourceRetriever resourceRetriever =
            new DefaultResourceRetriever(2000, 2000); // timeouts

    // Cache para JWKS (Google recomienda cachear)
    private final Map<String, RSAKey> keyCache = new ConcurrentHashMap<>();

    public AuthUser validate(String idToken) {

        try {
            // Parsear token
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            JWSHeader header = signedJWT.getHeader();
            String keyId = header.getKeyID();
            JWSAlgorithm algorithm = header.getAlgorithm();
            if (!algorithm.equals(JWSAlgorithm.RS256)) {
                throw new IllegalArgumentException("Algoritmo no soportado: " + algorithm);
            }

            // Obtener clave pública desde cache o Google JWKS
            RSAKey rsaKey = keyCache.computeIfAbsent(keyId, this::fetchKeyFromGoogle);
            if (rsaKey == null) {
                throw new IllegalStateException("No se encontró clave con key id: " + keyId);
            }
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

            // Verificar firma
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                throw new SecurityException("Firma del token inválida");
            }

            // Validar expiración
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration.before(new Date())) {
                throw new SecurityException("Token expirado");
            }

            // Extraer claims
            String sub = signedJWT.getJWTClaimsSet().getSubject();
            String email = signedJWT.getJWTClaimsSet().getStringClaim("email");
            String name = signedJWT.getJWTClaimsSet().getStringClaim("name");
            String picture = signedJWT.getJWTClaimsSet().getStringClaim("picture");

            return new AuthUser(sub, email, name, picture);

        } catch (Exception e) {
            throw new RuntimeException("Validate error Google ID Token: " + e.getMessage(), e);
        }
    }

    private RSAKey fetchKeyFromGoogle(String kid) {
        try {
            URI uri = URI.create(GOOGLE_JWKS_URL);
            String jwksJson = resourceRetriever
                    .retrieveResource(uri.toURL())
                    .getContent();
            JWKSet jwkSet = JWKSet.parse(jwksJson);
            for (JWK jwk : jwkSet.getKeys()) {
                if (jwk.getKeyID().equals(kid) && jwk instanceof RSAKey rsaKey) {
                    return rsaKey;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error descargando JWKS de Google", e);
        }
    }
}
