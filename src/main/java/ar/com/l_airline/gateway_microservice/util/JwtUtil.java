package ar.com.l_airline.gateway_microservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Responsible for handling operations related to JWT (JSON Web Tokens),
 * such as validation, role extraction, and email retrieval.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Retrieves the signing key used to verify JWTs.
     * The key is derived from a Base64-encoded secret stored in application properties.
     *
     * @return the HMAC SHA key used for JWT signature verification
     */
    public SecretKey getSingKey() {
        byte[] key = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(key);
    }

    /**
     * Validates a given JWT.
     * If the token is invalid or expired, a JwtException will be thrown.
     *
     * @param token the JWT to validate
     * @throws JwtException if the token is malformed, expired, or otherwise invalid
     */
    public void validateToken(String token) throws JwtException {
        Jwts.parser().verifyWith(getSingKey()).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Determines whether the user associated with the given JWT has an ADMIN role.
     *
     * @param token the JWT containing user claims
     * @return true if the role claim equals "ADMIN"; false otherwise
     */
    public boolean isAdmin(String token){
        Claims claims = Jwts.parser().verifyWith(getSingKey()).build().parseSignedClaims(token).getPayload();

        String role = (String) claims.get("role");
        return role.matches("ADMIN");
    }

    /**
     * Extracts the email (subject) from the JWT.
     * If the token has expired, the method still retrieves the subject from the exception's claims.
     *
     * @param token the JWT from which to extract the email
     * @return the email (subject) if available; a default message otherwise
     */
    public String getEmail(String token){
        try {
            Jwts.parser().verifyWith(getSingKey()).build().parseSignedClaims(token).getPayload();
        }catch (ExpiredJwtException e){
            return e.getClaims().getSubject();
        }
        return "No se ha encontrado payload.";
    }

}
