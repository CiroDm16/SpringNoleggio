package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;

    @Column(unique = true, nullable = false)
    private String licensePlate;

    private Double dailyPrice;
    private Boolean available = true;

    @ManyToOne
    @JoinColumn(name = "user_username", referencedColumnName = "username")
    private User user;

}