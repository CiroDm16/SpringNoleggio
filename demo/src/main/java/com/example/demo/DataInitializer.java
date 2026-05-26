package com.example.demo;

import com.example.demo.model.*;
import com.example.demo.repository.CarRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CarRepository carRepository;

    public DataInitializer(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Creiamo un'auto di prova per testare il DB
        if (carRepository.count() == 0) {
            Car c1 = new Car();
            c1.setBrand("Fiat");
            c1.setModel("500");
            c1.setLicensePlate("AB123CD");
            c1.setDailyPrice(45.0);
            c1.setAvailable(true);

            carRepository.save(c1);
            System.out.println("Database inizializzato: Auto di prova inserita!");
        }
    }
}