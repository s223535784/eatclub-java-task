package com.eatclub.deals.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Maps the JSON response from external API
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDataResponse {
    private List<Restaurant> restaurants;
}