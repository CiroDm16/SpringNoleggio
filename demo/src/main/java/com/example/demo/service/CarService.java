package com.example.demo.service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.CarDTO;
import com.example.demo.dto.UpdatedCarDTO;
import com.example.demo.model.Car;
import com.example.demo.model.User;
import com.example.demo.repository.CarRepository;
import com.example.demo.repository.UserRepository;

/**
 * Service per la gestione del ciclo di vita delle auto e della logica di noleggio.
 */
@Service
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random RANDOM = new Random();

    public CarService(CarRepository carRepository, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    /**
     * Genera una targa univoca nel formato AA000AA.
     * Verifica la disponibilità sul database prima di confermarla.
     */
    public String generateLicensePlate() {
        String plate;
        boolean exists;

        do {
            StringBuilder sb = new StringBuilder();
            sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
            sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
            sb.append(String.format("%03d", RANDOM.nextInt(1000)));
            sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
            sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
            
            plate = sb.toString();
            exists = carRepository.existsByLicensePlate(plate);
        } while (exists);

        return plate;
    }

    /**
     * Crea una nuova auto assegnandole automaticamente una targa generata.
     */
    public CarDTO saveCar(CarDTO carDTO) {
        Car carEntity = new Car();
        carEntity.setBrand(carDTO.getBrand());
        carEntity.setModel(carDTO.getModel());
        carEntity.setDailyPrice(carDTO.getDailyPrice());
        carEntity.setAvailable(carDTO.getAvailable());
        carEntity.setLicensePlate(generateLicensePlate());

        return mapToDTO(carRepository.save(carEntity));
    }

    /**
     * Restituisce l'elenco di tutte le auto mappate in DTO.
     */
    public List<CarDTO> getAllCars() {
        return carRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gestisce il noleggio di un'auto associandola a un utente specifico.
     * Lancia eccezione se l'auto è già occupata o inesistente.
     */
    public CarDTO rentCar(String licensePlate, String username) {
        Car car = carRepository.findByLicensePlate(licensePlate);
        if (car == null) throw new RuntimeException("Auto non trovata.");
        if (!car.getAvailable()) throw new RuntimeException("Auto già noleggiata.");

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utente non trovato."));

        car.setAvailable(false);
        car.setUser(user);
        
        return mapToDTO(carRepository.save(car));
    }

    /**
     * Libera l'auto dal noleggio corrente e la rende nuovamente disponibile.
     */
    public CarDTO deRentCar(String licensePlate) {
        Car car = carRepository.findByLicensePlate(licensePlate);
        if (car == null) throw new RuntimeException("Auto non trovata.");
        if (car.getAvailable()) throw new RuntimeException("L'auto è già disponibile.");

        car.setAvailable(true);
        car.setUser(null); // Rimuove il legame con l'utente corrente
        
        return mapToDTO(carRepository.save(car));
    }

    /**
     * Aggiorna il costo giornaliero di noleggio per l'auto specificata.
     */
    public CarDTO updatePrice(String licensePlate, UpdatedCarDTO updatedCarDTO) {
        Car car = carRepository.findByLicensePlate(licensePlate);
        if (car == null) throw new RuntimeException("Auto non trovata.");
        
        car.setDailyPrice(updatedCarDTO.getDailyPrice());
        return mapToDTO(carRepository.save(car));
    }

    /**
     * Utility per la conversione da Entity a DTO.
     */
    private CarDTO mapToDTO(Car car) {
        CarDTO dto = new CarDTO();
        dto.setBrand(car.getBrand());
        dto.setModel(car.getModel());
        dto.setDailyPrice(car.getDailyPrice());
        dto.setAvailable(car.getAvailable());
        dto.setLicensePlate(car.getLicensePlate());
        dto.setUser(car.getUser());
        return dto;
    }
}