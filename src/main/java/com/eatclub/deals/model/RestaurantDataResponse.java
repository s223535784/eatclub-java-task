package com.eatclub.deals.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper class for the external API response containing list of restaurants.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDataResponse {
    private List<Restaurant> restaurants;
}