package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service per la gestione del ciclo di vita degli utenti e dell'autenticazione.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Valida le credenziali di accesso.
     * @return Il ruolo dell'utente se l'autenticazione ha successo, altrimenti null.
     */
    public String login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .map(User::getRole)
                .orElse(null);
    }

    /**
     * Registra un nuovo utente nel sistema assegnandogli il ruolo predefinito 'USER'.
     */
    public void register(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // In produzione, ricordati di usare BCryptPasswordEncoder
        user.setRole("USER");
        
        userRepository.save(user);
    }

    /**
     * Eleva i permessi di un utente esistente al ruolo di Amministratore.
     * @return Il nuovo ruolo assegnato o null se l'utente non esiste.
     */
    public String enableAdmin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole("ADMIN");
            userRepository.save(user);
            return user.getRole();
        }
        
        return null;
    }
}