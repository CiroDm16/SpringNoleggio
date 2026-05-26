package com.example.demo.controller;

import com.example.demo.dto.CarDTO;
import com.example.demo.dto.UpdatedCarDTO;
import com.example.demo.security.JwtUtils;
import com.example.demo.service.CarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per la gestione del parco auto e delle operazioni di noleggio.
 */
@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;
    private final JwtUtils jwtUtils;

    public CarController(CarService carService, JwtUtils jwtUtils) {
        this.carService = carService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Recupera la lista completa delle auto disponibili e noleggiate.
     */
    @GetMapping("/all")
    public ResponseEntity<List<CarDTO>> getAllCars() {
        List<CarDTO> cars = carService.getAllCars();
        return new ResponseEntity<>(cars, HttpStatus.OK);
    }

    /**
     * Registra una nuova auto nel sistema. Accesso riservato agli amministratori.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCar(
            @RequestBody CarDTO carDTO,
            @RequestHeader("Authorization") String authHeader) {
        
        // Verifica validità del token e permessi ADMIN
        if (isNotAuthorized(authHeader, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato: richiesti privilegi di amministratore");
        }

        CarDTO savedCar = carService.saveCar(carDTO);
        return new ResponseEntity<>(savedCar, HttpStatus.OK);
    }

    /**
     * Avvia la procedura di noleggio per un'auto specifica tramite targa.
     */
    @PatchMapping("/rent/{licensePlate}")
    public ResponseEntity<?> rentCar(
            @PathVariable String licensePlate,
            @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token mancante");
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sessione scaduta o non valida");
        }

        // Identifica l'utente dal token per associarlo al noleggio
        String username = jwtUtils.getUsernameFromToken(token);
        return ResponseEntity.ok(carService.rentCar(licensePlate, username));
    }

    /**
     * Finalizza il noleggio rendendo l'auto nuovamente disponibile. Accesso riservato ADMIN.
     */
    @PatchMapping("/return/{licensePlate}")
    public ResponseEntity<?> returnCar(
            @PathVariable String licensePlate,
            @RequestHeader("Authorization") String authHeader) {

        if (isNotAuthorized(authHeader, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato");
        }

        return new ResponseEntity<>(carService.deRentCar(licensePlate), HttpStatus.OK);
    }

    /**
     * Aggiorna il prezzo di noleggio di un'auto esistente. Accesso riservato ADMIN.
     */
    @PatchMapping("/update/price/{licensePlate}")
    public ResponseEntity<?> updatePrice(
            @PathVariable String licensePlate, 
            @RequestBody UpdatedCarDTO updatedCarDTO,
            @RequestHeader("Authorization") String authHeader) {
        
        if (isNotAuthorized(authHeader, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato");
        }

        return new ResponseEntity<>(carService.updatePrice(licensePlate, updatedCarDTO), HttpStatus.OK);
    }

    /**
     * Metodo di utility per centralizzare il controllo dei permessi.
     * In una versione avanzata, questa logica verrebbe gestita da Spring Security.
     */
    private boolean isNotAuthorized(String authHeader, String requiredRole) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return true;
        String token = authHeader.substring(7);
        return !jwtUtils.validateToken(token) || !jwtUtils.getRoleFromToken(token).equals(requiredRole);
    }
}