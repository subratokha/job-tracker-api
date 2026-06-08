package com.jobtracker.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String email, Long userId) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .claim("userId",userId)
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token, String email) {
        try {
            String emailFromToken = extractEmailFromToken(token);
            Date expiration = extractExpirationFromToken(token);
            Date now = new Date();
            return !expiration.before(now) && email.equals(emailFromToken);
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmailFromToken(String token) {
        return getPayload(token)
                .getSubject();
    }

    private Claims getPayload(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build().parseSignedClaims(token)
                .getPayload();
    }

    private Date extractExpirationFromToken(String token) {
        return getPayload(token)
                .getExpiration();
    }
}
