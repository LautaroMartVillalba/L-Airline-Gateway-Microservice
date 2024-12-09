package ar.com.l_airline.gateway_microservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public SecretKey getSingKey() {
        byte[] key = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(key);
    }

    public void validateToken(String token) {
        Jwts.parser().verifyWith(getSingKey()).build().parseSignedClaims(token).getPayload();
    }

    public boolean isAdmin(String token){
        Claims claims = Jwts.parser().verifyWith(getSingKey()).build().parseSignedClaims(token).getPayload();

        String role = (String) claims.get("role");
        return role.matches("ADMIN");
    }

}
