package com.example.demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility per la gestione dei JSON Web Token (JWT).
 * Si occupa della generazione, decodifica e validazione dei token per l'autenticazione.
 */
@Component
public class JwtUtils {

    // Configurazione della sicurezza: Chiave segreta e durata del token (24 ore)
    private final String SECRET = "LaMiaChiaveSegretissimaESuperLungaPerJWT2026";
    private final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private final long EXPIRATION_TIME = 86400000; 

    /**
     * Genera un token JWT contenente l'username come 'subject' e il ruolo come 'claim' personalizzato.
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY)
                .compact();
    }

    /**
     * Estrae l'identificativo dell'utente (username) dal payload del token.
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Estrae il ruolo dell'utente dai claim del token.
     */
    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    /**
     * Verifica l'integrità e la validità temporale del token.
     * @return true se il token è valido e non scaduto, altrimenti false.
     */
    public boolean validateToken(String token) {
        try {
            // Se il parsing ha successo, la firma e la scadenza sono valide
            Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Il fallimento indica token manomesso, scaduto o malformato
            return false;
        }
    }
}