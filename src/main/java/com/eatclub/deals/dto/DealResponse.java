package com.eatclub.deals.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single deal in the API response.
 * Note: 'restarantSuburb' is intentionally misspelled to match the specification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealResponse {
    
    private String restaurantObjectId;
    private String restaurantName;
    private String restaurantAddress1;
    
    // Note: Intentionally misspelled to match the specification
    @JsonProperty("restarantSuburb")
    private String restarantSuburb;
    
    private String restaurantOpen;
    private String restaurantClose;
    private String dealObjectId;
    private String discount;
    private String dineIn;
    private String lightning;
    private String qtyLeft;
}