package com.eatclub.deals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application class for EatClub Restaurant Deals API.
 * This application provides endpoints to query restaurant deals based on time
 * and calculate peak deal availability windows.
 */
@SpringBootApplication
public class EatClubDealsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EatClubDealsApplication.class, args);
    }
}