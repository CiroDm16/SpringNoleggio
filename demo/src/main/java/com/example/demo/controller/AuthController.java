package com.example.demo.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.security.JwtUtils;
import com.example.demo.service.UserService;

/**
 * Controller per la gestione dell'autenticazione, registrazione e permessi utente.
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Autentica un utente e restituisce un token JWT se le credenziali sono corrette.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String role = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

        if (role != null) {
            String token = jwtUtils.generateToken(loginRequest.getUsername(), role);
            
            return ResponseEntity.ok(Map.of(
                "token", token,
                "type", "Bearer",
                "role", role
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali errate");
    }

    /**
     * Registra un nuovo utente nel sistema con ruolo standard.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        userService.register(registerRequest.getUsername(), registerRequest.getPassword());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Promuove un utente al ruolo di Amministratore. 
     * Operazione protetta: richiede un token JWT valido di un utente già ADMIN.
     */
    @PatchMapping("/admin/enable/{username}")
    public ResponseEntity<?> enableAdmin(
            @PathVariable String username,
            @RequestHeader("Authorization") String authHeader) {

        // Validazione formale dell'header di autorizzazione
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token mancante");
        }

        String token = authHeader.substring(7);

        // Controllo validità e scadenza del token
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
        }

        // SICUREZZA: Solo un ADMIN esistente può abilitarne un altro
        if (!"ADMIN".equals(jwtUtils.getRoleFromToken(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Permessi insufficienti");
        }
  
        String role = userService.enableAdmin(username);

        if (role != null) {
            return ResponseEntity.ok(Map.of("role", role));
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}