package com.example.demo.dto;

import com.example.demo.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarDTO {
    private String brand;
    private String model;
    private Double dailyPrice;
    private Boolean available;
    private String licensePlate;
    private User user;
}